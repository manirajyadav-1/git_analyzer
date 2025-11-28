package com.archeologist.service;

import com.archeologist.entity.CodeAnalysis;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommitEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(CommitEmbeddingService.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Saves one commit embedding in its own transaction with retry logic.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveCommitEmbeddingWithRetry(CodeAnalysis analysis, String commitHash, String commitMessage, List<Double> embedding) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                String embeddingString = "[" + String.join(",", embedding.stream().map(String::valueOf).toArray(String[]::new)) + "]";

                // Use named parameter for embedding and cast inside SQL
                String sql = "INSERT INTO commit_embeddings (code_analysis_id, commit_hash, commit_message, embedding) " +
                        "VALUES (:analysisId, :commitHash, :commitMessage, CAST(:embedding AS vector))";

                entityManager.createNativeQuery(sql)
                        .setParameter("analysisId", analysis.getId())
                        .setParameter("commitHash", commitHash)
                        .setParameter("commitMessage", commitMessage)
                        .setParameter("embedding", embeddingString)
                        .executeUpdate();

                return; // success

            } catch (Exception e) {
                retryCount++;
                if (retryCount == maxRetries) {
                    logger.error("Failed to save embedding after {} retries for commitHash={}: {}",
                            maxRetries, commitHash, e.getMessage());
                    throw e;
                }
                logger.warn("Retry {} of {} for commitHash={}", retryCount, maxRetries, commitHash);
                try {
                    Thread.sleep(1000 * retryCount); // exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }
}
