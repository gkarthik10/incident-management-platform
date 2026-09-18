package com.karthik.incidentmanagement.dto;

import com.karthik.incidentmanagement.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusDto {

    @NotNull
    private Status status;
}