package com.sanctions.screening.controller;

import com.sanctions.screening.dto.ImportJobDto;
import com.sanctions.screening.dto.ImportStatusDto;
import com.sanctions.screening.importer.XmlSanctionsImporter.ImportResult;
import com.sanctions.screening.service.SanctionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations (requires ADMIN role)")
public class AdminController {

    private final SanctionsService sanctionsService;

    @PostMapping("/sync")
    @Operation(summary = "Trigger manual synchronization", description = "Manually trigger import of UN sanctions list")
    public ResponseEntity<ImportResult> triggerSync() {
        String triggeredBy = "admin"; // In production, use authenticated user
        ImportResult result = sanctionsService.triggerImport(triggeredBy);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sync/status")
    @Operation(summary = "Get import status", description = "Get current import status and statistics")
    public ResponseEntity<ImportStatusDto> getSyncStatus() {
        ImportStatusDto status = sanctionsService.getImportStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/sync/history")
    @Operation(summary = "Get import history", description = "Get recent import job history")
    public ResponseEntity<List<ImportJobDto>> getSyncHistory() {
        List<ImportJobDto> history = sanctionsService.getImportHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/sync/jobs/{jobUuid}")
    @Operation(summary = "Get import job details", description = "Get details of a specific import job")
    public ResponseEntity<ImportJobDto> getSyncJob(@PathVariable String jobUuid) {
        return sanctionsService.getImportJob(jobUuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
