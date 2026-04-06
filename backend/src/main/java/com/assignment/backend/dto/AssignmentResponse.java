package com.assignment.backend.dto;

import com.assignment.backend.model.AssignmentStatus;

import java.time.OffsetDateTime;

public class AssignmentResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String studentUsername;
    private final OffsetDateTime submissionDate;
    private final AssignmentStatus status;
    private final ReviewResponse review;

    public AssignmentResponse(Long id, String title, String description, String studentUsername,
                              OffsetDateTime submissionDate, AssignmentStatus status, ReviewResponse review) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.studentUsername = studentUsername;
        this.submissionDate = submissionDate;
        this.status = status;
        this.review = review;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public OffsetDateTime getSubmissionDate() {
        return submissionDate;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public ReviewResponse getReview() {
        return review;
    }
}
