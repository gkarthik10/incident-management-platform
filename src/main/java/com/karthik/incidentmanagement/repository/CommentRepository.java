package com.karthik.incidentmanagement.repository;

import com.karthik.incidentmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findByIncidentId(Long incidentId);
}