package com.archeologist.controller;

import com.archeologist.entity.CodeAnalysis;
import com.archeologist.service.AnalysisService;
import com.archeologist.service.GitHubService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeRepository(@RequestBody Map<String, String> request) {
        String repoUrl = request.get("repoUrl");
//        String sessionId = session.getId();

        logger.info("Received /api/analyze request with repoUrl: {} ", repoUrl);

        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "repoUrl is required."));
        }

        try {
            Map<String, String> repoInfo = gitHubService.extractRepoInfo(repoUrl);
            String owner = repoInfo.get("owner");
            String repo = repoInfo.get("repo");

            int totalCommits = gitHubService.getTotalCommitCount(owner, repo);
            List<Map<String, Object>> contributors = gitHubService.fetchContributors(owner, repo);
            List<Map<String, Object>> commitActivity = gitHubService.fetchCommits(owner, repo, 100);
            Map<String, Integer> fileChanges = gitHubService.fetchFileChanges(owner, repo);
            List<Map<String, Object>> issues = gitHubService.fetchIssues(owner, repo);
            Map<String, String> dependencies = gitHubService.fetchDependencies(owner, repo);

            // Convert data to JSON strings for storage
            CodeAnalysis analysis = new CodeAnalysis();
//            analysis.setSessionId(sessionId);
            analysis.setRepoUrl(repoUrl);
            analysis.setStatus("COMPLETED");
            analysis.setCommits(objectMapper.writeValueAsString(Map.of("totalCommits", totalCommits)));
            analysis.setContributors(objectMapper.writeValueAsString(contributors));
            analysis.setCommitActivity(objectMapper.writeValueAsString(commitActivity));
            analysis.setFileChanges(objectMapper.writeValueAsString(fileChanges));
            analysis.setIssues(objectMapper.writeValueAsString(issues));
            analysis.setDependencies(objectMapper.writeValueAsString(dependencies));

            CodeAnalysis saved = analysisService.saveAnalysis(analysis);

//            session.setAttribute("analysisId", saved.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Analysis completed successfully",
                    "analysisId", saved.getId(),
                    "totalCommits", totalCommits
            ));

        } catch (Exception e) {
            logger.error("Error initializing analysis:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Failed to initialize analysis."));
        }
    }

    @PostMapping("/get-total-commits")
    public ResponseEntity<Map<String, Object>> getTotalCommits(@RequestBody Map<String, String> request) {
        String repoUrl = request.get("repoUrl");
        try {
            Map<String, String> repoInfo = gitHubService.extractRepoInfo(repoUrl);
            int totalCommits = gitHubService.getTotalCommitCount(repoInfo.get("owner"), repoInfo.get("repo"));
            return ResponseEntity.ok(Map.of("totalCommits", totalCommits));
        } catch (Exception e) {
            logger.error("Error fetching total commits", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch total commits"));
        }
    }

    @PostMapping("/process-commits")
    public ResponseEntity<Map<String, Object>> processCommits(@RequestBody Map<String, Object> request) {
        Long analysisId = Long.valueOf(request.get("analysisId").toString());
        Integer commitCount = Integer.valueOf(request.get("commitCount").toString());

        try {
            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "Analysis not found"
                ));
            }

            CodeAnalysis analysis = analysisOpt.get();
            Map<String, String> repoInfo = gitHubService.extractRepoInfo(analysis.getRepoUrl());
            String owner = repoInfo.get("owner");
            String repo = repoInfo.get("repo");

            List<Map<String, Object>> commits = gitHubService.fetchCommits(owner, repo, commitCount);


            List<Map<String, Object>> processedCommits = commits.stream()
                    .map(commit -> {
                        Map<String, Object> processedCommit = new HashMap<>();
                        processedCommit.put("sha", commit.get("sha"));
                        processedCommit.put("message", commit.get("message"));

                        // Extract author information
                        if (commit.containsKey("author")) {
                            Map<String, Object> author = (Map<String, Object>) commit.get("author");
                            processedCommit.put("author", Map.of(
                                    "name", author.get("name"),
                                    "email", author.get("email"),
                                    "date", author.get("date")
                            ));
                        }

                        return processedCommit;
                    })
                    .collect(java.util.stream.Collectors.toList());

            // Update commits as JSON
            Map<String, Object> commitsData = Map.of(
                    "totalCommits", commitCount,
                    "commits", processedCommits
            );
            analysis.setCommits(objectMapper.writeValueAsString(commitsData));
            analysis.setStatus("completed");

            analysisService.saveAnalysis(analysis);

            // Process embeddings in background
            try {
                // Use a separate thread to process embeddings
                new Thread(() -> {
                    try {
                        analysisService.processCommitEmbeddings(analysisId, commits);
                    } catch (Exception e) {
                        logger.warn("Background embedding processing failed: {}", e.getMessage());
                    }
                }).start();
            } catch (Exception e) {
                logger.warn("Failed to start background embedding processing: {}", e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Commits processed successfully",
                    "processedCommits", processedCommits.size()
            ));

        } catch (Exception e) {
            logger.error("Error processing commits:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/analysis-data")
    public ResponseEntity<Map<String, Object>> getAnalysisData(@RequestParam Long analysisId) {
        try {
            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "Analysis not found"
                ));
            }

            CodeAnalysis analysis = analysisOpt.get();

            // Deserialize JSON strings
            Map<String, Object> commits = parseJson(analysis.getCommits());
            Map<String, Object> fileChanges = parseJson(analysis.getFileChanges());
            Object commitActivity = parseJsonList(analysis.getCommitActivity());
            Object contributors = parseJsonList(analysis.getContributors());
            Object dependencies = parseJson(analysis.getDependencies());
            Object issues = parseJsonList(analysis.getIssues());

            // Get commit data from commits JSON
            List<Map<String, Object>> commitData = new ArrayList<>();
            if (commits.containsKey("commits")) {
                commitData = (List<Map<String, Object>>) commits.get("commits");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", Map.of(
                    "id", analysis.getId(),
                    "repo_url", analysis.getRepoUrl(),
                    "status", analysis.getStatus(),
                    "created_at", analysis.getCreatedAt(),
                    "codeEvolution", commitData,
                    "file_changes", fileChanges,
                    "commit_activity", commitActivity,
                    "contributors", contributors,
                    "dependencies", dependencies,
                    "issues", issues
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving analysis data:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<CodeAnalysis> getAnalysis(@PathVariable Long analysisId) {
        return analysisService.getAnalysisById(analysisId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @GetMapping("/session")
//    public ResponseEntity<Map<String, Object>> getSession(HttpSession session) {
//        String sessionId = session.getId();
//        Long analysisId = (Long) session.getAttribute("analysisId");
//
//        try {
//            if (analysisId != null) {
//                // Verify analysis exists
//                Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
//                if (analysisOpt.isPresent()) {
//                    return ResponseEntity.ok(Map.of(
//                            "sessionId", sessionId,
//                            "analysisId", analysisOpt.get().getId()
//                    ));
//                }
//            }
//
//            return ResponseEntity.ok(Map.of(
//                    "sessionId", sessionId,
//                    "analysisId", null
//            ));
//        } catch (Exception e) {
//            logger.error("Session error:", e);
//            return ResponseEntity.status(500).body(Map.of("error", "Session error"));
//        }
//    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }

    @GetMapping("/db-health")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        try {
            // Simple database connectivity test
            Optional<CodeAnalysis> testAnalysis = analysisService.getAnalysisById(1L);
            return ResponseEntity.ok(Map.of(
                    "status", "healthy",
                    "database", "connected",
                    "message", "Database connection is working"
            ));
        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "unhealthy",
                    "database", "disconnected",
                    "error", e.getMessage()
            ));
        }
    }

    // ---------------- Helper methods ----------------
    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
