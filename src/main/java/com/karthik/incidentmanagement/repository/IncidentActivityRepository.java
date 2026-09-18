package com.karthik.incidentmanagement.repository;

import com.karthik.incidentmanagement.entity.IncidentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentActivityRepository extends JpaRepository<IncidentActivity, Long> {

    List<IncidentActivity> findByIncidentIdOrderByTimestampDesc(Long incidentId);

    void deleteByIncidentId(Long incidentId);
}