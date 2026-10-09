package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_query", nullable = false, length = 1000)
    private String searchQuery;

    @Column(name = "search_query_normalized", length = 1000)
    private String searchQueryNormalized;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type_filter")
    private RecordType recordTypeFilter;

    @Column(name = "results_count")
    private int resultsCount = 0;

    @Column(name = "min_score_filter")
    private Double minScoreFilter;

    @Column(name = "search_duration_ms")
    private long searchDurationMs;

    @Column(name = "ai_analysis_enabled")
    private boolean aiAnalysisEnabled = false;

    @Column(name = "ai_analysis_duration_ms")
    private Long aiAnalysisDurationMs;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "user_id", length = 100)
    private String userId;
}
