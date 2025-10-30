package com.archeologist.controller;

import com.archeologist.service.AnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    @Autowired
    private AnalysisService analysisService;
    
    private Long getAnalysisIdFromSession(HttpSession session) {
        Long analysisId = (Long) session.getAttribute("analysisId");
        if (analysisId == null) {
            throw new RuntimeException("No analysis found for session");
        }
        return analysisId;
    }
    
    @GetMapping("/search-commits")
    public ResponseEntity<Map<String, Object>> searchCommits(@RequestParam String query, @RequestParam(name = "analysisId") Long analysisId) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Search query is required"));
        }
        
        try {
            List<Map<String, Object>> results = analysisService.searchSimilarCommits(analysisId, query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("results", results);
            
            if (results.isEmpty()) {
                response.put("message", "No matching commits found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Search error:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Failed to perform semantic search", "details", e.getMessage()));
        }
    }
    
    @PostMapping("/question-answering")
    public ResponseEntity<Map<String, Object>> answerQuestion(@RequestBody Map<String, String> request, @RequestParam(name = "analysisId") Long analysisId) {
        
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Valid question is required"));
        }
        
        try {
            String answer = analysisService.generateAnswer(analysisId, question);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("answer", answer);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Question answering error:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Failed to process question"));
        }
    }
    
    @PostMapping("/summarize")
    public ResponseEntity<Map<String, Object>> summarize(@RequestParam(name = "analysisId") Long analysisId) {
        try {
            String summary = analysisService.generateSummary(analysisId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Summarization error:", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Failed to generate summary"));
        }
    }
}
