package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.CommentRequestDto;
import com.karthik.incidentmanagement.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {

    CommentResponseDto addComment(Long incidentId, CommentRequestDto dto, String authorEmail);

    List<CommentResponseDto> getComments(Long incidentId);
}
