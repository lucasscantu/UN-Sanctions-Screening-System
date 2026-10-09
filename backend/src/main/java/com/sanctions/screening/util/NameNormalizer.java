package com.sanctions.screening.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class NameNormalizer {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}']+");
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\p{L}\\p{N}]+");

    public String normalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String normalized = input;
        
        // Normalize Unicode (NFKD decomposes accented characters)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD);
        
        // Remove diacritics (combining characters)
        normalized = normalized.replaceAll("\\p{M}", "");
        
        // Convert to lowercase
        normalized = normalized.toLowerCase();
        
        // Replace punctuation and special characters with spaces
        normalized = PUNCTUATION_PATTERN.matcher(normalized).replaceAll(" ");
        
        // Replace non-word characters with spaces
        normalized = NON_WORD_PATTERN.matcher(normalized).replaceAll(" ");
        
        // Collapse multiple whitespace into single space
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");
        
        // Trim leading/trailing whitespace
        normalized = normalized.trim();

        return normalized;
    }

    public String normalizeForComparison(String input) {
        String normalized = normalize(input);
        // Remove all non-alphanumeric characters except spaces
        return normalized.replaceAll("[^a-z0-9 ]", "").trim();
    }

    public String extractTokens(String input) {
        String normalized = normalizeForComparison(input);
        // Split on whitespace and filter empty tokens
        return String.join(" ", normalized.split("\\s+"));
    }
}
