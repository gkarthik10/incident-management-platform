package com.karthik.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRequestDto {

    @NotBlank
    private String description;
}