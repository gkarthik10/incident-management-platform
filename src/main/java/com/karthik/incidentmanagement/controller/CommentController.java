package com.karthik.incidentmanagement.controller;

import com.karthik.incidentmanagement.dto.CommentRequestDto;
import com.karthik.incidentmanagement.dto.CommentResponseDto;
import com.karthik.incidentmanagement.security.SecurityUtils;
import com.karthik.incidentmanagement.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDto dto) {

        CommentResponseDto comment = commentService.addComment(id, dto, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.status(201).body(comment);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','EMPLOYEE')")
    @GetMapping("/{id}/comments")
    public List<CommentResponseDto> getComments(@PathVariable Long id) {
        return commentService.getComments(id);
    }
}
