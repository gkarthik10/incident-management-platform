package com.karthik.incidentmanagement.dto;

import com.karthik.incidentmanagement.entity.ActivityAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentActivityDto {

    private Long id;
    private ActivityAction action;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;
}
