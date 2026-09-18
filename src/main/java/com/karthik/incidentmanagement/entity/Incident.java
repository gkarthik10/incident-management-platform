package com.karthik.incidentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private Status status;

    /** The user who reported the incident. Set automatically from the authenticated principal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    /** The engineer currently responsible for resolving the incident. Null until assigned. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // --- AI triage results, populated asynchronously right after creation ---
    private String aiCategory;

    private String aiSeverity;

    @Column(length = 1000)
    private String aiRecommendation;

    private LocalDateTime aiAnalyzedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Optimistic locking: guards against two engineers overwriting each other's status/assignment updates. */
    @Version
    private Long version;

    @Builder.Default
    @OneToMany(
            mappedBy = "incident",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();
}
