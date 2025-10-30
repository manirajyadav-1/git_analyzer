package com.archeologist.service;

import com.archeologist.entity.CodeAnalysis;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    
    @Autowired
    private GitHub gitHub;
    
    public Map<String, String> extractRepoInfo(String repoUrl) {
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        if (parts.length >= 2) {
            return Map.of("owner", parts[0], "repo", parts[1]);
        }
        throw new IllegalArgumentException("Invalid GitHub repository URL");
    }
    
    public int getTotalCommitCount(String owner, String repo) throws IOException {
        GHRepository repository = gitHub.getRepository(owner + "/" + repo);
        return repository.listCommits().toList().size();
    }
    
    public List<Map<String, Object>> fetchContributors(String owner, String repo) throws IOException {
        GHRepository repository = gitHub.getRepository(owner + "/" + repo);
        return repository.listContributors().toList().stream()
                .map(contributor -> {
                    Map<String, Object> contrib = new HashMap<>();
                    contrib.put("login", contributor.getLogin());
                    contrib.put("contributions", contributor.getContributions());
                    contrib.put("avatar_url", contributor.getAvatarUrl());
                    return contrib;
                })
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> fetchCommits(String owner, String repo, int limit) throws IOException {
        GHRepository repository = gitHub.getRepository(owner + "/" + repo);
        return repository.listCommits().toList().stream()
                .limit(limit)
                .map(commit -> {
                    Map<String, Object> commitData = new HashMap<>();
                    commitData.put("sha", commit.getSHA1());
                    try {
                        commitData.put("message", commit.getCommitShortInfo().getMessage());
                        commitData.put("author", Map.of(
                                "name", commit.getCommitShortInfo().getAuthor().getName(),
                                "email", commit.getCommitShortInfo().getAuthor().getEmail(),
                                "date", commit.getCommitShortInfo().getAuthor().getDate()
                        ));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return commitData;
                })
                .collect(Collectors.toList());
    }
    
    public Map<String, Integer> fetchFileChanges(String owner, String repo) throws IOException {
        GHRepository repository = gitHub.getRepository(owner + "/" + repo);
        Map<String, Integer> fileChangeCounts = new HashMap<>();
        
        repository.listCommits().toList().stream()
                .limit(100) // Limit to avoid rate limits
                .forEach(commit -> {
                    try {
                        commit.getFiles().forEach(file -> {
                            String filename = file.getFileName();
                            fileChangeCounts.merge(filename, 1, Integer::sum);
                        });
                    } catch (IOException e) {
                        logger.warn("Error processing commit files: {}", e.getMessage());
                    }
                });
        
        return fileChangeCounts;
    }
    
    public List<Map<String, Object>> fetchIssues(String owner, String repo) throws IOException {
        GHRepository repository = gitHub.getRepository(owner + "/" + repo);
        return repository.getIssues(GHIssueState.ALL).stream()
                .map(issue -> {
                    Map<String, Object> issueData = new HashMap<>();
                    issueData.put("id", issue.getId());
                    issueData.put("number", issue.getNumber());
                    issueData.put("title", issue.getTitle());
                    issueData.put("state", issue.getState().toString());
                    try {
                        issueData.put("created_at", issue.getCreatedAt());
                        issueData.put("updated_at", issue.getUpdatedAt());
                        issueData.put("closed_at", issue.getClosedAt());
                        issueData.put("url", issue.getHtmlUrl().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return issueData;
                })
                .collect(Collectors.toList());
    }
    
    public Map<String, String> fetchDependencies(String owner, String repo) throws IOException {
        try {
            GHRepository repository = gitHub.getRepository(owner + "/" + repo);
            GHContent packageJson = repository.getFileContent("package.json");
            String content = new String(packageJson.read().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            
            // Parse JSON manually or use a JSON library
            // For simplicity, returning empty map - in production, use Jackson or Gson
            return new HashMap<>();
        } catch (Exception e) {
            logger.warn("Could not fetch package.json for {}/{}: {}", owner, repo, e.getMessage());
            return new HashMap<>();
        }
    }
}
