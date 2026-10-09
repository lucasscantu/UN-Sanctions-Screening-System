package com.sanctions.screening.matching;

import com.sanctions.screening.util.NameNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarityCalculatorTest {

    @Mock
    private NameNormalizer nameNormalizer;

    @InjectMocks
    private SimilarityCalculator similarityCalculator;

    @BeforeEach
    void setUp() {
        when(nameNormalizer.normalizeForComparison(anyString())).thenAnswer(inv -> {
            String input = inv.getArgument(0);
            return input != null ? input.toLowerCase().replaceAll("[^a-z0-9]", "") : "";
        });
        when(nameNormalizer.normalize(anyString())).thenAnswer(inv -> {
            String input = inv.getArgument(0);
            return input != null ? input.toLowerCase() : "";
        });
    }

    @Test
    void testExactMatch() {
        double similarity = similarityCalculator.calculateSimilarity("John Doe", "John Doe");
        assertEquals(100.0, similarity, 0.01);
    }

    @Test
    void testCaseInsensitiveMatch() {
        double similarity = similarityCalculator.calculateSimilarity("John Doe", "JOHN DOE");
        assertEquals(100.0, similarity, 0.01);
    }

    @Test
    void testPartialMatch() {
        double similarity = similarityCalculator.calculateSimilarity("John Doe", "John");
        assertTrue(similarity > 50.0);
    }

    @Test
    void testDifferentNames() {
        double similarity = similarityCalculator.calculateSimilarity("John Doe", "Jane Smith");
        assertTrue(similarity < 50.0);
    }

    @Test
    void testLevenshteinSimilarity() {
        // Identical strings
        assertEquals(1.0, similarityCalculator.calculateLevenshteinSimilarity("test", "test"), 0.001);
        
        // Completely different
        assertEquals(0.0, similarityCalculator.calculateLevenshteinSimilarity("abc", "xyz"), 0.001);
        
        // One character difference
        double similarity = similarityCalculator.calculateLevenshteinSimilarity("test", "tent");
        assertTrue(similarity > 0.5 && similarity < 1.0);
    }

    @Test
    void testJaroWinklerSimilarity() {
        // Identical strings
        assertEquals(1.0, similarityCalculator.calculateJaroWinklerSimilarity("test", "test"), 0.001);
        
        // Similar strings with prefix match
        double similarity = similarityCalculator.calculateJaroWinklerSimilarity("MARTHA", "MARHTA");
        assertTrue(similarity > 0.9);
    }

    @Test
    void testTokenBasedSimilarity() {
        // Identical tokens
        assertEquals(1.0, similarityCalculator.calculateTokenBasedSimilarity("john doe", "john doe"), 0.001);
        
        // Partial overlap
        double similarity = similarityCalculator.calculateTokenBasedSimilarity("john doe", "john smith");
        assertEquals(0.5, similarity, 0.001);
        
        // No overlap
        assertEquals(0.0, similarityCalculator.calculateTokenBasedSimilarity("john doe", "jane smith"), 0.001);
    }

    @Test
    void testAliasBoost() {
        // No aliases
        assertEquals(0.0, similarityCalculator.calculateAliasBoost("John Doe", "John Doe", null, null), 0.001);
        
        // With matching alias
        double boost = similarityCalculator.calculateAliasBoost(
                "John Doe", "John Doe", 
                null, 
                List.of("John Doe", "John D."));
        assertTrue(boost > 0.0);
    }

    @Test
    void testDetailedSimilarity() {
        SanctionedRecordCandidate candidate = SanctionedRecordCandidate.builder()
                .id(1L)
                .primaryName("John Doe")
                .aliases(List.of("Johnny Doe", "John D."))
                .build();

        SimilarityCalculator.SimilarityResult result = similarityCalculator.calculateDetailedSimilarity(
                "John Doe", candidate);

        assertNotNull(result);
        assertEquals("John Doe", result.searchName());
        assertEquals("John Doe", result.candidateName());
        assertTrue(result.combinedScore() >= 0 && result.combinedScore() <= 100);
    }

    @Test
    void testNullInputs() {
        assertEquals(0.0, similarityCalculator.calculateSimilarity(null, "test"));
        assertEquals(0.0, similarityCalculator.calculateSimilarity("test", null));
        assertEquals(0.0, similarityCalculator.calculateSimilarity(null, null));
    }

    @Test
    void testEmptyInputs() {
        assertEquals(0.0, similarityCalculator.calculateSimilarity("", "test"));
        assertEquals(0.0, similarityCalculator.calculateSimilarity("test", ""));
        assertEquals(100.0, similarityCalculator.calculateSimilarity("", ""));
    }
}
