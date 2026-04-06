package com.assignment.backend.service;

import com.assignment.backend.dto.AssignmentCreateRequest;
import com.assignment.backend.dto.AssignmentResponse;
import com.assignment.backend.dto.ReviewRequest;
import com.assignment.backend.dto.ReviewResponse;
import com.assignment.backend.exception.ResourceNotFoundException;
import com.assignment.backend.metrics.AppMetricsService;
import com.assignment.backend.model.Assignment;
import com.assignment.backend.model.AssignmentStatus;
import com.assignment.backend.model.Review;
import com.assignment.backend.model.UserAccount;
import com.assignment.backend.repository.AssignmentRepository;
import com.assignment.backend.repository.ReviewRepository;
import com.assignment.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AppMetricsService metricsService;

    public AssignmentService(AssignmentRepository assignmentRepository, ReviewRepository reviewRepository,
                             UserRepository userRepository, AppMetricsService metricsService) {
        this.assignmentRepository = assignmentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.metricsService = metricsService;
    }

    @Transactional
    public AssignmentResponse createAssignment(String username, AssignmentCreateRequest request) {
        UserAccount student = getUser(username);
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setStudent(student);
        assignment.setSubmissionDate(OffsetDateTime.now());
        assignment.setStatus(AssignmentStatus.SUBMITTED);
        Assignment saved = assignmentRepository.save(assignment);
        metricsService.incrementAssignmentSubmissions();
        log.info("event=assignment_submitted assignmentId={} student={}", saved.getId(), username);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForStudent(String username) {
        return assignmentRepository.findAllByStudentOrderBySubmissionDateDesc(getUser(username)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Assignment::getSubmissionDate).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssignmentResponse reviewAssignment(Long assignmentId, String reviewerUsername, ReviewRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        UserAccount reviewer = getUser(reviewerUsername);
        Review review = assignment.getReview();
        if (review == null) {
            review = new Review();
            review.setAssignment(assignment);
        }

        review.setReviewer(reviewer);
        review.setFeedback(request.getFeedback());
        review.setGrade(request.getGrade());
        review.setReviewedAt(OffsetDateTime.now());
        assignment.setStatus(AssignmentStatus.REVIEWED);
        assignment.setReview(review);
        reviewRepository.save(review);
        Assignment saved = assignmentRepository.save(assignment);
        metricsService.incrementReviews();
        log.info("event=assignment_reviewed assignmentId={} reviewer={}", assignmentId, reviewerUsername);
        return toResponse(saved);
    }

    private UserAccount getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        ReviewResponse reviewResponse = null;
        if (assignment.getReview() != null) {
            reviewResponse = new ReviewResponse(
                    assignment.getReview().getReviewer().getUsername(),
                    assignment.getReview().getFeedback(),
                    assignment.getReview().getGrade(),
                    assignment.getReview().getReviewedAt()
            );
        }
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStudent().getUsername(),
                assignment.getSubmissionDate(),
                assignment.getStatus(),
                reviewResponse
        );
    }
}
