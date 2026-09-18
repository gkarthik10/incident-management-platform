package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.AiResponseDto;
import com.karthik.incidentmanagement.entity.ActivityAction;
import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.IncidentActivity;
import com.karthik.incidentmanagement.repository.IncidentActivityRepository;
import com.karthik.incidentmanagement.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Runs AI triage in the background right after an incident is created, so
 * the reporter gets an instant API response while the LLM call (which can
 * take a second or two) happens off the request thread.
 * <p>
 * Listens for {@link IncidentCreatedEvent} with {@code phase = AFTER_COMMIT}
 * rather than being called directly from IncidentServiceImpl, for two
 * reasons: (1) it decouples "an incident was created" from "AI triage should
 * run", which is the standard event-driven way to add side effects without
 * bloating the core service; and (2) it guarantees the incident row is
 * actually committed and visible before this (async, different-thread) code
 * tries to read and update it — calling an @Async method directly from
 * inside an open transaction risks the triage thread running before commit.
 */
@Service
@RequiredArgsConstructor
public class IncidentTriageService {

    private static final Logger log = LoggerFactory.getLogger(IncidentTriageService.class);

    private final AiService aiService;
    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;

    @Async("aiTriageExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIncidentCreated(IncidentCreatedEvent event) {
        triage(event.incidentId(), event.description());
    }

    void triage(Long incidentId, String description) {

        AiResponseDto result = aiService.analyze(description);

        incidentRepository.findById(incidentId).ifPresentOrElse(incident -> {
            incident.setAiCategory(result.getCategory());
            incident.setAiSeverity(result.getSeverity());
            incident.setAiRecommendation(result.getRecommendation());
            incident.setAiAnalyzedAt(LocalDateTime.now());
            incidentRepository.save(incident);

            activityRepository.save(IncidentActivity.builder()
                    .incident(incident)
                    .action(ActivityAction.AI_ANALYZED)
                    .performedBy("SYSTEM")
                    .details("AI suggested category=%s, severity=%s".formatted(result.getCategory(), result.getSeverity()))
                    .timestamp(LocalDateTime.now())
                    .build());

        }, () -> log.warn("Incident {} was deleted before AI triage completed — discarding result", incidentId));
    }
}
