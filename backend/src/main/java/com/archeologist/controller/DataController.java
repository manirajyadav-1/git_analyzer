package com.archeologist.controller;

import com.archeologist.entity.CodeAnalysis;
import com.archeologist.service.AnalysisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DataController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long getAnalysisIdFromSession(HttpSession session) {
        Long analysisId = (Long) session.getAttribute("analysisId");
        if (analysisId == null) {
            logger.warn("No analysisId found in session {}", session.getId());
            return null;
        }
        return analysisId;
    }

    private Long resolveAnalysisId(HttpSession session, Long analysisIdParam) {
        if (analysisIdParam != null) {
            return analysisIdParam;
        }
        return getAnalysisIdFromSession(session);
    }

    private ResponseEntity<Map<String, Object>> successWithData(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, Exception e) {
        logger.error(message, e);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @GetMapping("/file-change-frequency")
    public ResponseEntity<Map<String, Object>> getFileChangeFrequency(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching file change frequency for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                // return empty list so frontend shows empty chart instead of error message
                return successWithData(Collections.emptyList());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);

            if (analysisOpt.isEmpty() || analysisOpt.get().getFileChanges() == null) {
                return successWithData(Collections.emptyList());
            }

            return successWithData(analysisOpt.get().getFileChanges());

        } catch (Exception e) {
            return buildErrorResponse("Error fetching file changes", e);
        }
    }

    @GetMapping("/commit-activity-timeline")
    public ResponseEntity<Map<String, Object>> getCommitActivityTimeline(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching commit activity timeline for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                return successWithData(Collections.emptyList());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty() || analysisOpt.get().getCommitActivity() == null) {
                return successWithData(Collections.emptyList());
            }

            return successWithData(analysisOpt.get().getCommitActivity());

        } catch (Exception e) {
            return buildErrorResponse("Error fetching commit activity", e);
        }
    }

    @GetMapping("/contributor-statistics")
    public ResponseEntity<Map<String, Object>> getContributorStatistics(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching contributor statistics for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                return successWithData(Collections.emptyList());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty() || analysisOpt.get().getContributors() == null) {
                return successWithData(Collections.emptyList());
            }

            return successWithData(analysisOpt.get().getContributors());

        } catch (Exception e) {
            return buildErrorResponse("Error fetching contributors", e);
        }
    }

    @GetMapping("/codebase-heatmap")
    public ResponseEntity<Map<String, Object>> getCodebaseHeatmap(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching codebase heatmap for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                return successWithData(Collections.emptyMap());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty() || analysisOpt.get().getFileChanges() == null) {
                return successWithData(Collections.emptyMap());
            }

            return successWithData(analysisOpt.get().getFileChanges());

        } catch (Exception e) {
            return buildErrorResponse("Error generating heatmap", e);
        }
    }

    @GetMapping("/dependency-graph")
    public ResponseEntity<Map<String, Object>> getDependencyGraph(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching dependency graph for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                return successWithData(Collections.emptyMap());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty() || analysisOpt.get().getDependencies() == null) {
                return successWithData(Collections.emptyMap());
            }

            // Deserialize JSON string into Map
            Map<String, Object> dependencies = objectMapper.readValue(
                    analysisOpt.get().getDependencies(), new TypeReference<>() {});

            return successWithData(dependencies);

        } catch (Exception e) {
            return buildErrorResponse("Error fetching dependencies", e);
        }
    }

    @GetMapping("/linked-issues")
    public ResponseEntity<Map<String, Object>> getLinkedIssues(HttpSession session, @RequestParam(name = "analysisId", required = false) Long analysisIdParam) {

        logger.info("Fetching linked issues for session {}", session.getId());
        try {
            Long analysisId = resolveAnalysisId(session, analysisIdParam);
            if (analysisId == null) {
                return successWithData(Collections.emptyList());
            }

            Optional<CodeAnalysis> analysisOpt = analysisService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty() || analysisOpt.get().getIssues() == null) {
                return successWithData(Collections.emptyList());
            }

            return successWithData(analysisOpt.get().getIssues());

        } catch (Exception e) {
            return buildErrorResponse("Error fetching issues", e);
        }
    }
}