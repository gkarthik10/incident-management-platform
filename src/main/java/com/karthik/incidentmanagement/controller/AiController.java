package com.karthik.incidentmanagement.controller;

import com.karthik.incidentmanagement.dto.AiRequestDto;
import com.karthik.incidentmanagement.dto.AiResponseDto;
import com.karthik.incidentmanagement.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze")
    public ResponseEntity<AiResponseDto> analyze(
            @Valid @RequestBody AiRequestDto request) {

        return ResponseEntity.ok(
                aiService.analyze(request.getDescription())
        );
    }
}