package com.sanctions.screening.controller;

import com.sanctions.screening.dto.AIAnalysisResult;
import com.sanctions.screening.dto.SearchRequest;
import com.sanctions.screening.dto.SearchResult;
import com.sanctions.screening.service.SanctionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Sanctions list search operations")
public class SearchController {

    private final SanctionsService sanctionsService;

    @GetMapping
    @Operation(summary = "Search sanctions list", description = "Search for individuals and entities in the UN sanctions list")
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "0") Double minScore,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        
        SearchRequest request = SearchRequest.builder()
                .name(name)
                .minScore(minScore)
                .page(page)
                .pageSize(pageSize)
                .build();

        SearchResult result = sanctionsService.searchSanctions(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Search with POST", description = "Search for individuals and entities using POST request")
    public ResponseEntity<SearchResult> searchPost(@RequestBody SearchRequest request) {
        SearchResult result = sanctionsService.searchSanctions(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ai-analysis")
    @Operation(summary = "Get AI analysis for search results", description = "Perform AI analysis on search results for linguistic analysis")
    public CompletableFuture<ResponseEntity<List<AIAnalysisResult>>> analyzeWithAI(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "0") Double minScore) {
        
        SearchRequest request = SearchRequest.builder()
                .name(name)
                .minScore(minScore)
                .page(1)
                .pageSize(10) // Limit for AI analysis
                .build();

        SearchResult searchResult = sanctionsService.searchSanctions(request);
        return sanctionsService.analyzeWithAI(searchResult.getResults())
                .thenApply(results -> ResponseEntity.ok(results));
    }
}
