package com.assignment.backend.dto;

import java.time.OffsetDateTime;

public class ReviewResponse {

    private final String reviewerUsername;
    private final String feedback;
    private final Integer grade;
    private final OffsetDateTime reviewedAt;

    public ReviewResponse(String reviewerUsername, String feedback, Integer grade, OffsetDateTime reviewedAt) {
        this.reviewerUsername = reviewerUsername;
        this.feedback = feedback;
        this.grade = grade;
        this.reviewedAt = reviewedAt;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public String getFeedback() {
        return feedback;
    }

    public Integer getGrade() {
        return grade;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }
}
