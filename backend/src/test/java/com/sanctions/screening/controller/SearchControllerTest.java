package com.sanctions.screening.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctions.screening.dto.SearchRequest;
import com.sanctions.screening.dto.SearchResult;
import com.sanctions.screening.service.SanctionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SanctionsService sanctionsService;

    @InjectMocks
    private SearchController searchController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(sanctionsService.searchSanctions(any(SearchRequest.class)))
                .thenReturn(createMockSearchResult());
    }

    @Test
    void testSearchGet() {
        ResponseEntity<SearchResult> response = searchController.search(
                "John Doe", null, 0.0, 1, 20);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchPost() throws Exception {
        SearchRequest request = SearchRequest.builder()
                .name("John Doe")
                .minScore(60.0)
                .page(1)
                .pageSize(20)
                .build();
        
        ResponseEntity<SearchResult> response = searchController.searchPost(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchEmptyQuery() {
        ResponseEntity<SearchResult> response = searchController.search("", null, 0.0, 1, 20);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalResults());
    }

    private SearchResult createMockSearchResult() {
        return SearchResult.builder()
                .query("test")
                .normalizedQuery("test")
                .totalResults(0)
                .results(Collections.emptyList())
                .searchDurationMs(100)
                .candidatesRetrieved(0)
                .build();
    }
}
