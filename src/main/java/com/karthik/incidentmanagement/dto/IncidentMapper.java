package com.karthik.incidentmanagement.dto;

import com.karthik.incidentmanagement.entity.Comment;
import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.User;

public final class IncidentMapper {

    private IncidentMapper() {
    }

    public static UserSummaryDto toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public static IncidentResponseDto toDto(Incident incident) {
        return IncidentResponseDto.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .reportedBy(toUserSummary(incident.getReportedBy()))
                .assignedTo(toUserSummary(incident.getAssignedTo()))
                .aiCategory(incident.getAiCategory())
                .aiSeverity(incident.getAiSeverity())
                .aiRecommendation(incident.getAiRecommendation())
                .aiAnalyzedAt(incident.getAiAnalyzedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    public static CommentResponseDto toDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .message(comment.getMessage())
                .incidentId(comment.getIncident().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
