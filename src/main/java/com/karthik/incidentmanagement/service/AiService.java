package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.AiResponseDto;

public interface AiService {

    AiResponseDto analyze(String description);
}