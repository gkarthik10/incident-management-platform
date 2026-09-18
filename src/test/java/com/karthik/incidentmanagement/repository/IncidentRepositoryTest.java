package com.karthik.incidentmanagement.repository;

import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.Role;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import com.karthik.incidentmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real Spring Data JPA layer — including the dynamic
 * {@link IncidentSpecifications} filter — against an actual (in-memory H2)
 * database, rather than mocking the repository away.
 */
@DataJpaTest
@ActiveProfiles("test")
class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    private User reporter;

    @BeforeEach
    void setUp() {
        reporter = userRepository.save(User.builder()
                .name("Reporter")
                .email("reporter@example.com")
                .password("hashed")
                .role(Role.EMPLOYEE)
                .build());

        saveIncident("DB down", Severity.CRITICAL, Status.OPEN);
        saveIncident("Slow checkout", Severity.MEDIUM, Status.OPEN);
        saveIncident("Typo on landing page", Severity.LOW, Status.RESOLVED);
    }

    private void saveIncident(String title, Severity severity, Status status) {
        incidentRepository.save(Incident.builder()
                .title(title)
                .description(title + " - description")
                .severity(severity)
                .status(status)
                .reportedBy(reporter)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    void findAllWithSpecification_filtersByStatusAndSeverityIndependently() {
        Page<Incident> openOnly = incidentRepository.findAll(
                IncidentSpecifications.withFilters(Status.OPEN, null), PageRequest.of(0, 10));
        assertThat(openOnly.getTotalElements()).isEqualTo(2);

        Page<Incident> criticalOnly = incidentRepository.findAll(
                IncidentSpecifications.withFilters(null, Severity.CRITICAL), PageRequest.of(0, 10));
        assertThat(criticalOnly.getTotalElements()).isEqualTo(1);
        assertThat(criticalOnly.getContent().get(0).getTitle()).isEqualTo("DB down");

        Page<Incident> combined = incidentRepository.findAll(
                IncidentSpecifications.withFilters(Status.OPEN, Severity.MEDIUM), PageRequest.of(0, 10));
        assertThat(combined.getTotalElements()).isEqualTo(1);
        assertThat(combined.getContent().get(0).getTitle()).isEqualTo("Slow checkout");
    }

    @Test
    void findAllWithSpecification_returnsEverything_whenNoFiltersGiven() {
        Page<Incident> all = incidentRepository.findAll(
                IncidentSpecifications.withFilters(null, null), PageRequest.of(0, 10));

        assertThat(all.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findByReportedById_isScopedToTheGivenUser() {
        User otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@example.com")
                .password("hashed")
                .role(Role.EMPLOYEE)
                .build());

        incidentRepository.save(Incident.builder()
                .title("Someone else's incident")
                .description("n/a")
                .severity(Severity.LOW)
                .status(Status.OPEN)
                .reportedBy(otherUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Page<Incident> mine = incidentRepository.findByReportedById(reporter.getId(), PageRequest.of(0, 10));

        assertThat(mine.getTotalElements()).isEqualTo(3);
        assertThat(mine.getContent()).allMatch(i -> i.getReportedBy().getId().equals(reporter.getId()));
    }

    @Test
    void pagination_returnsCorrectPageSizeAndTotalCount() {
        Page<Incident> firstPage = incidentRepository.findAll(
                IncidentSpecifications.withFilters(null, null), PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
