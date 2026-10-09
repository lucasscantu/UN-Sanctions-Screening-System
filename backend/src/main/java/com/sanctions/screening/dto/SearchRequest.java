package com.sanctions.screening.dto;

import com.sanctions.screening.entity.RecordType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    @Size(min = 1, max = 1000, message = "Search name must be between 1 and 1000 characters")
    private String name;

    private RecordType recordType;

    @Min(value = 0, message = "Minimum score must be at least 0")
    @Max(value = 100, message = "Minimum score must be at most 100")
    private Double minScore;

    @Min(value = 1, message = "Page must be at least 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must be at most 100")
    private Integer pageSize = 20;

    private Boolean includeInactive = false;
}
