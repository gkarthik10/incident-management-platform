package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.IncidentRequestDto;
import com.karthik.incidentmanagement.dto.IncidentResponseDto;
import com.karthik.incidentmanagement.dto.UpdateStatusDto;
import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.Role;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import com.karthik.incidentmanagement.entity.User;
import com.karthik.incidentmanagement.exception.ResourceNotFoundException;
import com.karthik.incidentmanagement.repository.IncidentActivityRepository;
import com.karthik.incidentmanagement.repository.IncidentRepository;
import com.karthik.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private User reporter;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .id(10L)
                .name("Reporter")
                .email("reporter@example.com")
                .role(Role.EMPLOYEE)
                .build();
    }

    @Test
    void createIncident_persistsIncident_logsActivity_andPublishesTriageEvent() {
        IncidentRequestDto dto = new IncidentRequestDto();
        dto.setTitle("Payment service down");
        dto.setDescription("5xx errors on checkout since 10:14 UTC");
        dto.setSeverity(Severity.HIGH);

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        // The repository .save(..) call assigns the ID in real life; the mock
        // just echoes back whatever entity it was given.
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident incident = inv.getArgument(0);
            incident.setId(99L);
            return incident;
        });

        IncidentResponseDto result = incidentService.createIncident(dto, reporter.getEmail());

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);

        // Activity is logged synchronously...
        verify(activityRepository).save(any());

        // ...but AI triage is only ever triggered via the event, so a slow
        // or failing Groq call can never block or fail incident creation.
        ArgumentCaptor<IncidentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(IncidentCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().incidentId()).isEqualTo(99L);
    }

    @Test
    void getIncidentById_throwsNotFound_whenMissing() {
        when(incidentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getIncidentById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_updatesStatus_andRecordsActivityWithBeforeAndAfterValues() {
        Incident incident = Incident.builder()
                .id(5L)
                .title("DB latency spike")
                .severity(Severity.MEDIUM)
                .status(Status.OPEN)
                .build();

        UpdateStatusDto dto = new UpdateStatusDto();
        dto.setStatus(Status.IN_PROGRESS);

        when(incidentRepository.findById(5L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponseDto result = incidentService.updateStatus(5L, dto, "engineer@example.com");

        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);

        ArgumentCaptor<com.karthik.incidentmanagement.entity.IncidentActivity> activityCaptor =
                ArgumentCaptor.forClass(com.karthik.incidentmanagement.entity.IncidentActivity.class);
        verify(activityRepository).save(activityCaptor.capture());
        assertThat(activityCaptor.getValue().getDetails()).contains("OPEN").contains("IN_PROGRESS");
    }
}
