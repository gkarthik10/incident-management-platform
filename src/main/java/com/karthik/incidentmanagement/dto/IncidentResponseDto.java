package com.karthik.incidentmanagement.dto;

import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * What the API actually returns for an incident. Deliberately decoupled
 * from the Incident JPA entity so the database schema can evolve
 * independently of the public API contract, and so lazy associations
 * (comments) never accidentally leak or trigger LazyInitializationException.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentResponseDto {

    private Long id;
    private String title;
    private String description;
    private Severity severity;
    private Status status;

    private UserSummaryDto reportedBy;
    private UserSummaryDto assignedTo;

    private String aiCategory;
    private String aiSeverity;
    private String aiRecommendation;
    private LocalDateTime aiAnalyzedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
