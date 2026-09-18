package com.karthik.incidentmanagement.controller;

import com.karthik.incidentmanagement.dto.DashboardStatsDto;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import com.karthik.incidentmanagement.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IncidentRepository incidentRepository;

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping("/stats")
    public DashboardStatsDto getStats() {

        return new DashboardStatsDto(
                incidentRepository.count(),
                incidentRepository.countByStatus(Status.OPEN),
                incidentRepository.countByStatus(Status.IN_PROGRESS),
                incidentRepository.countByStatus(Status.RESOLVED),
                incidentRepository.countByStatus(Status.CLOSED),
                incidentRepository.countBySeverity(Severity.CRITICAL),
                incidentRepository.countBySeverity(Severity.HIGH)
        );
    }
}
