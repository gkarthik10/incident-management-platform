package com.karthik.incidentmanagement.service;

/**
 * Published the moment an incident is saved. Consumed by
 * {@link IncidentTriageService} only after the enclosing transaction commits
 * (see {@code @TransactionalEventListener(phase = AFTER_COMMIT)}), so the
 * async AI triage thread never races the creating transaction and tries to
 * read a row that isn't visible yet.
 */
public record IncidentCreatedEvent(Long incidentId, String description) {
}
