package com.karthik.incidentmanagement.repository;

import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.Severity;
import com.karthik.incidentmanagement.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentRepository
        extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatus(Status status);

    long countBySeverity(Severity severity);

    Page<Incident> findByReportedById(Long reportedById, Pageable pageable);

    Page<Incident> findByAssignedToId(Long assignedToId, Pageable pageable);
}
