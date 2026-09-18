package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.CommentRequestDto;
import com.karthik.incidentmanagement.dto.CommentResponseDto;
import com.karthik.incidentmanagement.dto.IncidentMapper;
import com.karthik.incidentmanagement.entity.ActivityAction;
import com.karthik.incidentmanagement.entity.Comment;
import com.karthik.incidentmanagement.entity.Incident;
import com.karthik.incidentmanagement.entity.IncidentActivity;
import com.karthik.incidentmanagement.exception.ResourceNotFoundException;
import com.karthik.incidentmanagement.repository.CommentRepository;
import com.karthik.incidentmanagement.repository.IncidentActivityRepository;
import com.karthik.incidentmanagement.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;

    @Override
    public CommentResponseDto addComment(Long incidentId, CommentRequestDto dto, String authorEmail) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

        Comment comment = Comment.builder()
                .message(dto.getMessage())
                .createdAt(LocalDateTime.now())
                .incident(incident)
                .build();

        comment = commentRepository.save(comment);

        activityRepository.save(IncidentActivity.builder()
                .incident(incident)
                .action(ActivityAction.COMMENTED)
                .performedBy(authorEmail)
                .details(truncate(dto.getMessage()))
                .timestamp(LocalDateTime.now())
                .build());

        return IncidentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long incidentId) {

        if (!incidentRepository.existsById(incidentId)) {
            throw new ResourceNotFoundException("Incident not found with id: " + incidentId);
        }

        return commentRepository.findByIncidentId(incidentId).stream()
                .map(IncidentMapper::toDto)
                .toList();
    }

    private String truncate(String message) {
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }
}
