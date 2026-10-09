package com.sanctions.screening.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameNormalizerTest {

    private final NameNormalizer normalizer = new NameNormalizer();

    @Test
    void testNormalizeBasic() {
        assertEquals("john doe", normalizer.normalize("John Doe"));
        assertEquals("john doe", normalizer.normalize("JOHN DOE"));
    }

    @Test
    void testNormalizeAccents() {
        assertEquals("jose", normalizer.normalize("José"));
        assertEquals("maria", normalizer.normalize("María"));
        assertEquals("francois", normalizer.normalize("François"));
    }

    @Test
    void testNormalizePunctuation() {
        assertEquals("john doe", normalizer.normalize("John, Doe"));
        assertEquals("john doe", normalizer.normalize("John - Doe"));
        assertEquals("john doe", normalizer.normalize("John's Doe"));
    }

    @Test
    void testNormalizeWhitespace() {
        assertEquals("john doe", normalizer.normalize("John  Doe"));
        assertEquals("john doe", normalizer.normalize("  John Doe  "));
        assertEquals("john doe", normalizer.normalize("John\t\nDoe"));
    }

    @Test
    void testNormalizeNull() {
        assertEquals("", normalizer.normalize(null));
    }

    @Test
    void testNormalizeEmpty() {
        assertEquals("", normalizer.normalize(""));
    }

    @Test
    void testNormalizeForComparison() {
        assertEquals("john doe", normalizer.normalizeForComparison("John Doe"));
        assertEquals("john doe", normalizer.normalizeForComparison("John-Doe"));
        assertEquals("john doe", normalizer.normalizeForComparison("John, Doe!"));
    }

    @Test
    void testExtractTokens() {
        assertEquals("john doe", normalizer.extractTokens("John Doe"));
        assertEquals("john doe", normalizer.extractTokens("John, Doe"));
        assertEquals("john doe", normalizer.extractTokens("  John   Doe  "));
    }

    @Test
    void testUnicodeNormalization() {
        // Test NFKD decomposition
        assertEquals("cafe", normalizer.normalize("café"));
        assertEquals("naive", normalizer.normalize("naïve"));
    }

    @Test
    void testMixedCaseAndSpecialChars() {
        assertEquals("john doe jr", normalizer.normalize("John Doe, Jr."));
        assertEquals("john doe jr", normalizer.normalize("JOHN DOE JR"));
    }
}
