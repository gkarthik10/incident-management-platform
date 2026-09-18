package com.karthik.incidentmanagement.repository;

import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds a single dynamic WHERE clause for GET /api/incidents so status and
 * severity filters can be combined freely without a repository method per
 * combination (findByStatus, findBySeverity, findByStatusAndSeverity, ...).
 */
public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> withFilters(Status status, Severity severity) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (severity != null) {
                predicate = cb.and(predicate, cb.equal(root.get("severity"), severity));
            }

            return predicate;
        };
    }
}
