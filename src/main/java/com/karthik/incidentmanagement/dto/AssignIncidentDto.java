package com.karthik.incidentmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignIncidentDto {

    @NotNull
    private Long engineerId;
}
