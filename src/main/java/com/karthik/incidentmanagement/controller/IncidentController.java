package com.karthik.incidentmanagement.controller;

import com.karthik.incidentmanagement.dto.AssignIncidentDto;
import com.karthik.incidentmanagement.dto.IncidentActivityDto;
import com.karthik.incidentmanagement.dto.IncidentRequestDto;
import com.karthik.incidentmanagement.dto.IncidentResponseDto;
import com.karthik.incidentmanagement.dto.UpdateStatusDto;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import com.karthik.incidentmanagement.security.SecurityUtils;
import com.karthik.incidentmanagement.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @PostMapping
    public ResponseEntity<IncidentResponseDto> createIncident(@Valid @RequestBody IncidentRequestDto dto) {

        IncidentResponseDto created = incidentService.createIncident(dto, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.status(201).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping
    public Page<IncidentResponseDto> getAllIncidents(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Severity severity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return incidentService.getAllIncidents(status, severity, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping("/my")
    public Page<IncidentResponseDto> getMyIncidents(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return incidentService.getMyIncidents(SecurityUtils.getCurrentUserEmail(), pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping("/{id}")
    public IncidentResponseDto getIncidentById(@PathVariable Long id) {
        return incidentService.getIncidentById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping("/{id}/activity")
    public List<IncidentActivityDto> getActivity(@PathVariable Long id) {
        return incidentService.getActivity(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER')")
    @PutMapping("/{id}/status")
    public IncidentResponseDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusDto dto) {
        return incidentService.updateStatus(id, dto, SecurityUtils.getCurrentUserEmail());
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER')")
    @PutMapping("/{id}/assign")
    public IncidentResponseDto assignIncident(@PathVariable Long id, @Valid @RequestBody AssignIncidentDto dto) {
        return incidentService.assignIncident(id, dto, SecurityUtils.getCurrentUserEmail());
    }
}
