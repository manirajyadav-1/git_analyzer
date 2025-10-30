package com.archeologist.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "commit_embeddings")
public class CommitEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_analysis_id")
    private CodeAnalysis codeAnalysis;

    @Column(name = "commit_hash", nullable = false)
    private String commitHash;

    @Column(name = "commit_message", nullable = false)
    private String commitMessage;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    @JdbcTypeCode(SqlTypes.OTHER)
    private String embedding;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public CommitEmbedding() {
    }

    public CommitEmbedding(CodeAnalysis codeAnalysis, String commitHash, String commitMessage, double[] embedding, LocalDateTime createdAt) {
        this.codeAnalysis = codeAnalysis;
        this.commitHash = commitHash;
        this.commitMessage = commitMessage;
        this.embedding = "[" + String.join(",", java.util.Arrays.stream(embedding).mapToObj(String::valueOf).toArray(String[]::new)) + "]";
        this.createdAt = createdAt;
    }

    public CommitEmbedding(CodeAnalysis analysis, String commitHash, String commitMessage, String embeddingString) {
        this.codeAnalysis = analysis;
        this.commitHash = commitHash;
        this.commitMessage = commitMessage;
        this.embedding = embeddingString;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CodeAnalysis getCodeAnalysis() {
        return codeAnalysis;
    }

    public void setCodeAnalysis(CodeAnalysis codeAnalysis) {
        this.codeAnalysis = codeAnalysis;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}