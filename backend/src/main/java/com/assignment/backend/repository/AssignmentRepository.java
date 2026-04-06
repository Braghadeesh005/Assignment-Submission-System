package com.assignment.backend.repository;

import com.assignment.backend.model.Assignment;
import com.assignment.backend.model.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = {"student", "review", "review.reviewer"})
    List<Assignment> findAllByStudentOrderBySubmissionDateDesc(UserAccount student);

    @Override
    @EntityGraph(attributePaths = {"student", "review", "review.reviewer"})
    List<Assignment> findAll();
}
