package com.sanctions.screening.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SanctionsConfig {

    @Value("${sanctions.xml.source-url:https://www.un.org/securitycouncil/content/un-sc-consolidated-list-xml}")
    private String xmlSourceUrl;

    @Value("${sanctions.xml.download-timeout-seconds:300}")
    private int downloadTimeoutSeconds;

    @Value("${sanctions.xml.sync-interval-hours:24}")
    private int syncIntervalHours;

    @Value("${sanctions.xml.max-records-per-import:10000}")
    private int maxRecordsPerImport;

    @Value("${search.candidate-limit:50}")
    private int candidateLimit;

    @Value("${search.similarity-threshold:60}")
    private double similarityThreshold;

    @Value("${search.min-score-filter:0}")
    private double minScoreFilter;

    @Value("${search.max-ai-candidates:10}")
    private int maxAiCandidates;

    @Bean
    public SanctionsProperties sanctionsProperties() {
        return new SanctionsProperties(
                xmlSourceUrl,
                downloadTimeoutSeconds,
                syncIntervalHours,
                maxRecordsPerImport
        );
    }

    @Bean
    public SearchProperties searchProperties() {
        return new SearchProperties(
                candidateLimit,
                similarityThreshold,
                minScoreFilter,
                maxAiCandidates
        );
    }

    public record SanctionsProperties(
            String xmlSourceUrl,
            int downloadTimeoutSeconds,
            int syncIntervalHours,
            int maxRecordsPerImport
    ) {}

    public record SearchProperties(
            int candidateLimit,
            double similarityThreshold,
            double minScoreFilter,
            int maxAiCandidates
    ) {}
}
