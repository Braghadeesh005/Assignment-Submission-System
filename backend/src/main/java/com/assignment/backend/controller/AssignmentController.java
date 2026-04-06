package com.assignment.backend.controller;

import com.assignment.backend.dto.AssignmentCreateRequest;
import com.assignment.backend.dto.AssignmentResponse;
import com.assignment.backend.dto.ReviewRequest;
import com.assignment.backend.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public AssignmentResponse createAssignment(@Valid @RequestBody AssignmentCreateRequest request,
                                               Authentication authentication) {
        return assignmentService.createAssignment(authentication.getName(), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<AssignmentResponse> getMyAssignments(Authentication authentication) {
        return assignmentService.getAssignmentsForStudent(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('REVIEWER')")
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('REVIEWER')")
    public AssignmentResponse reviewAssignment(@PathVariable Long id, @Valid @RequestBody ReviewRequest request,
                                               Authentication authentication) {
        return assignmentService.reviewAssignment(id, authentication.getName(), request);
    }
}
