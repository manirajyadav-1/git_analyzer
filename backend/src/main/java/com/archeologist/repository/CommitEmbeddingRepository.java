package com.archeologist.repository;

import com.archeologist.entity.CodeAnalysis;
import com.archeologist.entity.CommitEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitEmbeddingRepository extends JpaRepository<CommitEmbedding, Long> {
    
    List<CommitEmbedding> findByCodeAnalysisOrderByCreatedAtDesc(CodeAnalysis codeAnalysis);
    
    @Query(value = "SELECT commit_hash, commit_message, " +
                   "1 - (embedding <=> CAST(:queryEmbedding AS vector)) as similarity " +
                   "FROM commit_embeddings " +
                   "WHERE code_analysis_id = :analysisId " +
                   "AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) > 0.5 " +
                   "ORDER BY similarity DESC " +
                   "LIMIT 5", nativeQuery = true)
    List<Object[]> findSimilarCommits(@Param("analysisId") Long analysisId, @Param("queryEmbedding") String queryEmbedding);
    
    @Query("SELECT ce.commitMessage FROM CommitEmbedding ce WHERE ce.codeAnalysis.id = :analysisId ORDER BY ce.createdAt DESC")
    List<String> findCommitMessagesByAnalysisId(@Param("analysisId") Long analysisId);
    
    boolean existsByCodeAnalysisAndCommitHash(CodeAnalysis codeAnalysis, String commitHash);
}
