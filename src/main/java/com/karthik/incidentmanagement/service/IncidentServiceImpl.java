package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.AssignIncidentDto;
import com.karthik.incidentmanagement.dto.IncidentActivityDto;
import com.karthik.incidentmanagement.dto.IncidentMapper;
import com.karthik.incidentmanagement.dto.IncidentRequestDto;
import com.karthik.incidentmanagement.dto.IncidentResponseDto;
import com.karthik.incidentmanagement.dto.UpdateStatusDto;
import com.karthik.incidentmanagement.entity.ActivityAction;
import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.IncidentActivity;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import com.karthik.incidentmanagement.entity.User;
import com.karthik.incidentmanagement.exception.ResourceNotFoundException;
import com.karthik.incidentmanagement.repository.IncidentActivityRepository;
import com.karthik.incidentmanagement.repository.IncidentRepository;
import com.karthik.incidentmanagement.repository.IncidentSpecifications;
import com.karthik.incidentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public IncidentResponseDto createIncident(IncidentRequestDto dto, String reporterEmail) {

        User reporter = findUserByEmail(reporterEmail);

        Incident incident = Incident.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .status(Status.OPEN)
                .reportedBy(reporter)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        incident = incidentRepository.save(incident);

        logActivity(incident, ActivityAction.CREATED, reporterEmail,
                "Incident reported with severity " + incident.getSeverity());

        // Published now, but only delivered to IncidentTriageService after
        // this transaction commits (see @TransactionalEventListener there).
        // That guarantees the async triage thread never reads a row that
        // hasn't been committed yet.
        eventPublisher.publishEvent(new IncidentCreatedEvent(incident.getId(), incident.getDescription()));

        return IncidentMapper.toDto(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponseDto getIncidentById(Long id) {
        return IncidentMapper.toDto(findIncidentOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDto> getAllIncidents(Status status, Severity severity, Pageable pageable) {
        return incidentRepository
                .findAll(IncidentSpecifications.withFilters(status, severity), pageable)
                .map(IncidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDto> getMyIncidents(String reporterEmail, Pageable pageable) {
        User reporter = findUserByEmail(reporterEmail);
        return incidentRepository.findByReportedById(reporter.getId(), pageable).map(IncidentMapper::toDto);
    }

    @Override
    public void deleteIncident(Long id) {
        Incident incident = findIncidentOrThrow(id);
        incidentRepository.delete(incident);
    }

    @Override
    public IncidentResponseDto updateStatus(Long id, UpdateStatusDto dto, String actorEmail) {

        Incident incident = findIncidentOrThrow(id);
        Status previous = incident.getStatus();

        incident.setStatus(dto.getStatus());
        incident.setUpdatedAt(LocalDateTime.now());
        incident = incidentRepository.save(incident);

        logActivity(incident, ActivityAction.STATUS_CHANGED, actorEmail,
                "Status changed from " + previous + " to " + dto.getStatus());

        return IncidentMapper.toDto(incident);
    }

    @Override
    public IncidentResponseDto assignIncident(Long id, AssignIncidentDto dto, String actorEmail) {

        Incident incident = findIncidentOrThrow(id);

        User engineer = userRepository.findById(dto.getEngineerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + dto.getEngineerId()));

        incident.setAssignedTo(engineer);
        incident.setUpdatedAt(LocalDateTime.now());
        incident = incidentRepository.save(incident);

        logActivity(incident, ActivityAction.ASSIGNED, actorEmail,
                "Assigned to " + engineer.getEmail());

        return IncidentMapper.toDto(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentActivityDto> getActivity(Long id) {

        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident not found with id: " + id);
        }

        return activityRepository.findByIncidentIdOrderByTimestampDesc(id).stream()
                .map(a -> new IncidentActivityDto(a.getId(), a.getAction(), a.getPerformedBy(), a.getDetails(), a.getTimestamp()))
                .toList();
    }

    private void logActivity(Incident incident, ActivityAction action, String performedBy, String details) {
        activityRepository.save(IncidentActivity.builder()
                .incident(incident)
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
