package com.assignment.backend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AppMetricsService {

    private final Counter assignmentSubmissionCounter;
    private final Counter reviewCounter;
    private final Counter errorCounter;

    public AppMetricsService(MeterRegistry meterRegistry) {
        this.assignmentSubmissionCounter = Counter.builder("assignment_submissions_total")
                .description("Total assignment submissions")
                .register(meterRegistry);
        this.reviewCounter = Counter.builder("assignment_reviews_total")
                .description("Total assignment reviews")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("application_errors_total")
                .description("Total handled application errors")
                .register(meterRegistry);
    }

    public void incrementAssignmentSubmissions() {
        assignmentSubmissionCounter.increment();
    }

    public void incrementReviews() {
        reviewCounter.increment();
    }

    public void incrementErrors() {
        errorCounter.increment();
    }
}
