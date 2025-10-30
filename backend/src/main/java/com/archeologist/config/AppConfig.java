package com.archeologist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

@Configuration
public class AppConfig {
    
    @Value("${github.token}")
    private String githubToken;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public GitHub gitHub() throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(githubToken)
                .build();
    }
}
