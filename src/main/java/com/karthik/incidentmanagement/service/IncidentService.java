package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.AssignIncidentDto;
import com.karthik.incidentmanagement.dto.IncidentActivityDto;
import com.karthik.incidentmanagement.dto.IncidentRequestDto;
import com.karthik.incidentmanagement.dto.IncidentResponseDto;
import com.karthik.incidentmanagement.dto.UpdateStatusDto;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IncidentService {

    IncidentResponseDto createIncident(IncidentRequestDto dto, String reporterEmail);

    IncidentResponseDto getIncidentById(Long id);

    Page<IncidentResponseDto> getAllIncidents(Status status, Severity severity, Pageable pageable);

    Page<IncidentResponseDto> getMyIncidents(String reporterEmail, Pageable pageable);

    void deleteIncident(Long id);

    IncidentResponseDto updateStatus(Long id, UpdateStatusDto dto, String actorEmail);

    IncidentResponseDto assignIncident(Long id, AssignIncidentDto dto, String actorEmail);

    List<IncidentActivityDto> getActivity(Long id);
}
