package com.archeologist.service;

import com.archeologist.entity.CodeAnalysis;
import com.archeologist.repository.CodeAnalysisRepository;
import com.archeologist.repository.CommitEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private CodeAnalysisRepository codeAnalysisRepository;

    @Autowired
    private CommitEmbeddingRepository commitEmbeddingRepository;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private CommitEmbeddingService commitEmbeddingService; 

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Populates CodeAnalysis fields safely from a generic data map.
     */
    private void populateAnalysisFields(CodeAnalysis analysis, Map<String, Object> data) {
        analysis.setCommits(safeJsonString(data.get("commits")));
        analysis.setContributors(safeJsonString(data.get("contributors")));
        analysis.setCommitActivity(safeJsonString(data.get("commitActivity")));
        analysis.setFileChanges(safeJsonString(data.get("fileChanges")));
        analysis.setDependencies(safeJsonString(data.get("dependencies")));
        analysis.setIssues(safeJsonString(data.get("issues")));
    }

    /**
     * Dedicated method to save the analysis entity.
     */
    public CodeAnalysis saveAnalysis(CodeAnalysis analysis) {
        try {
            CodeAnalysis saved = codeAnalysisRepository.save(analysis);
            logger.info("Analysis saved successfully with ID={}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save analysis: {}", e.getMessage());
            throw new RuntimeException("Failed to save analysis: " + e.getMessage(), e);
        }
    }

    /**
     * Converts object (Map/List/etc.) safely to JSON-style string.
     */
    @SuppressWarnings("unchecked")
    private String safeJsonString(Object obj) {
        if (obj == null) return "{}";
        if (obj instanceof String) return (String) obj;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Failed to serialize field to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    public Optional<CodeAnalysis> getAnalysisById(Long analysisId) {
        logger.debug("Fetching analysis by ID={}", analysisId);
        try {
            return codeAnalysisRepository.findById(analysisId);
        } catch (Exception e) {
            logger.error("Error fetching analysis by ID={}: {}", analysisId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Processes commits, generating and saving embeddings one by one using a separate transactional service.
     */
    public void processCommitEmbeddings(Long analysisId, List<Map<String, Object>> commits) {
        logger.info("Processing commit embeddings for analysisId={}, totalCommits={}", analysisId, commits.size());

        CodeAnalysis analysis = codeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found for ID=" + analysisId));

        int processedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (Map<String, Object> commit : commits) {
            try {
                String commitHash = (String) commit.get("sha");
                String commitMessage = (String) commit.get("message");

                if (commitHash == null || commitMessage == null) {
                    logger.warn("Skipping commit with missing SHA or message: {}", commit);
                    skippedCount++;
                    continue;
                }

                if (commitEmbeddingRepository.existsByCodeAnalysisAndCommitHash(analysis, commitHash)) {
                    logger.debug("Embedding already exists for commitHash={}", commitHash);
                    skippedCount++;
                    continue;
                }

                List<Double> embedding = openAIService.generateEmbedding(commitMessage);
                if (embedding == null || embedding.isEmpty()) {
                    logger.warn("No embedding generated for commitHash={}", commitHash);
                    errorCount++;
                    continue;
                }

                //  Each call runs in its own transaction
                commitEmbeddingService.saveCommitEmbeddingWithRetry(analysis, commitHash, commitMessage, embedding);
                processedCount++;

            } catch (Exception e) {
                logger.error("Error processing individual commit: {}", e.getMessage());
                errorCount++;
            }
        }

        logger.info("✅ Finished processing embeddings for analysisId={}. Processed: {}, Skipped: {}, Errors: {}",
                analysisId, processedCount, skippedCount, errorCount);
    }

    /**
     * Searches for similar commits using vector similarity.
     */
    public List<Map<String, Object>> searchSimilarCommits(Long analysisId, String query) {
        logger.info("Searching for similar commits in analysisId={} with query='{}'", analysisId, query);

        try {
            List<Double> queryEmbedding = openAIService.generateEmbedding(query);
            if (queryEmbedding == null) {
                logger.warn("Embedding generation failed for query='{}'", query);
                return new ArrayList<>();
            }

            String embeddingString = "[" + String.join(",", queryEmbedding.stream().map(String::valueOf).toArray(String[]::new)) + "]";
            List<Object[]> results = commitEmbeddingRepository.findSimilarCommits(analysisId, embeddingString);

            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> result = new HashMap<>();
                result.put("commit_hash", row[0]);
                result.put("commit_message", row[1]);
                result.put("similarity", row[2]);
                formattedResults.add(result);
            }

            return formattedResults;

        } catch (Exception e) {
            logger.error("Error searching similar commits for analysisId={}", analysisId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Generates answer for queries related to commit or any other
     */
    public String generateAnswer(Long analysisId, String question) {
        logger.info("Generating AI-based answer for analysisId={} and question='{}'", analysisId, question);

        try {
            List<String> commitMessages = commitEmbeddingRepository.findCommitMessagesByAnalysisId(analysisId);
            if (commitMessages.isEmpty()) {
                return "No commit data available for analysis.";
            }

            String prompt = String.format(
                    "You are an assistant analyzing commit messages.\n\nCommit History:\n%s\n\nQuestion: %s\n\nAnswer:",
                    String.join("\n", commitMessages.subList(0, Math.min(100, commitMessages.size()))),
                    question
            );

            return openAIService.generateCompletion(prompt);

        } catch (Exception e) {
            logger.error("Error generating answer for analysisId={}", analysisId, e);
            return "Error generating AI response: " + e.getMessage();
        }
    }

    /**
     * Generates commit summary
     */
    public String generateSummary(Long analysisId) {
        logger.info("Generating commit summary for analysisId={}", analysisId);

        try {
            List<String> commitMessages = commitEmbeddingRepository.findCommitMessagesByAnalysisId(analysisId);
            if (commitMessages.isEmpty()) {
                return "No commit messages to summarize.";
            }

            String prompt = String.format(
                    "Provide a concise summary of these commit messages:\n%s\n\nSummary:",
                    String.join("\n", commitMessages.subList(0, Math.min(500, commitMessages.size())))
            );

            return openAIService.generateCompletion(prompt);

        } catch (Exception e) {
            logger.error("Error generating summary for analysisId={}", analysisId, e);
            return "Error generating summary: " + e.getMessage();
        }
    }
}
