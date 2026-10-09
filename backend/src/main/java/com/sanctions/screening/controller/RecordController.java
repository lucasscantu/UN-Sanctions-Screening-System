package com.sanctions.screening.controller;

import com.sanctions.screening.dto.SanctionedRecordDto;
import com.sanctions.screening.service.SanctionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Records", description = "Sanctioned record operations")
public class RecordController {

    private final SanctionsService sanctionsService;

    @GetMapping("/{id}")
    @Operation(summary = "Get record by ID", description = "Retrieve a sanctioned record by its database ID")
    public ResponseEntity<SanctionedRecordDto> getRecordById(
            @Parameter(description = "Database ID of the record") @PathVariable Long id) {
        return sanctionsService.getRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get record by UN reference number", description = "Retrieve a sanctioned record by its UN reference number")
    public ResponseEntity<SanctionedRecordDto> getRecordByReference(
            @Parameter(description = "UN reference number") @PathVariable String referenceNumber) {
        return sanctionsService.getRecordByReference(referenceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all records", description = "Retrieve all sanctioned records with pagination")
    public ResponseEntity<Page<SanctionedRecordDto>> getAllRecords(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SanctionedRecordDto> records = sanctionsService.getAllRecords(pageable);
        return ResponseEntity.ok(records);
    }
}
