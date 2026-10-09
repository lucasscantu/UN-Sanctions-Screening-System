package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_uuid", unique = true, nullable = false, length = 50)
    private String jobUuid;

    @Column(name = "source_url", nullable = false, length = 2000)
    private String sourceUrl;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK

    @Column(name = "started_at")
    @CreationTimestamp
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "records_created")
    private int recordsCreated = 0;

    @Column(name = "records_updated")
    private int recordsUpdated = 0;

    @Column(name = "records_deactivated")
    private int recordsDeactivated = 0;

    @Column(name = "records_failed")
    private int recordsFailed = 0;

    @Column(name = "total_records_processed")
    private int totalRecordsProcessed = 0;

    @Column(name = "error_summary", columnDefinition = "TEXT")
    private String errorSummary;

    @Column(name = "file_size_bytes")
    private long fileSizeBytes;

    @Column(name = "file_checksum", length = 100)
    private String fileChecksum;

    @Column(name = "is_manual_trigger", nullable = false)
    private boolean isManualTrigger = false;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;
}
