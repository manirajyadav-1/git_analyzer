package com.archeologist.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_analysis")
public class CodeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "commits", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String commits = "{}";

    @Column(name = "file_changes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String fileChanges = "{}";

    @Column(name = "contributors", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String contributors = "{}";

    @Column(name = "commit_activity", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String commitActivity = "{}";

    @Column(name = "dependencies", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String dependencies = "{}";

    @Column(name = "issues", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String issues = "{}";

    public CodeAnalysis() {
    }

    public CodeAnalysis(String sessionId, String repoUrl, String status, LocalDateTime createdAt, String commits, String fileChanges, String contributors, String commitActivity, String dependencies, String issues) {
        this.sessionId = sessionId;
        this.repoUrl = repoUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.commits = commits;
        this.fileChanges = fileChanges;
        this.contributors = contributors;
        this.commitActivity = commitActivity;
        this.dependencies = dependencies;
        this.issues = issues;
    }

    public CodeAnalysis(String sessionId, String repoUrl, String status) {
        this.sessionId = sessionId;
        this.repoUrl = repoUrl;
        this.status = status;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCommits() {
        return commits;
    }

    public void setCommits(String commits) {
        this.commits = commits;
    }

    public String getFileChanges() {
        return fileChanges;
    }

    public void setFileChanges(String fileChanges) {
        this.fileChanges = fileChanges;
    }

    public String getContributors() {
        return contributors;
    }

    public void setContributors(String contributors) {
        this.contributors = contributors;
    }

    public String getCommitActivity() {
        return commitActivity;
    }

    public void setCommitActivity(String commitActivity) {
        this.commitActivity = commitActivity;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }
}