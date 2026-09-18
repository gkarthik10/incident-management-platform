package com.karthik.incidentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Append-only audit trail for an incident: every status change, assignment,
 * comment, and AI triage event is recorded here so the full history of an
 * incident can be reconstructed later ("who did what, when").
 */
@Entity
@Table(name = "incident_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @JsonIgnore
    private Incident incident;

    @Enumerated(EnumType.STRING)
    private ActivityAction action;

    /** Email of the user who performed the action, or "SYSTEM" for automated events (e.g. AI triage). */
    private String performedBy;

    @Column(length = 1000)
    private String details;

    private LocalDateTime timestamp;
}
