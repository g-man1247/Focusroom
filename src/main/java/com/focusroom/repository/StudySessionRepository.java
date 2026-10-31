package com.focusroom.repository;

import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.SessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    Optional<StudySession> findFirstByUserAndStatusOrderByStartTimeDesc(User user, SessionStatus status);

    List<StudySession> findByUserAndStatusOrderByStartTimeDesc(User user, SessionStatus status);

    List<StudySession> findByUserOrderByStartTimeDesc(User user, Pageable pageable);

    long countByUserAndStatus(User user, SessionStatus status);

    long countByStatus(SessionStatus status);

    /**
     * Demonstrates DBMS Aggregate Function: SUM() with COALESCE to handle nulls
     */
    @Query("SELECT COALESCE(SUM(s.actualDurationSeconds), 0) FROM StudySession s " +
           "WHERE s.user = :user AND s.status = 'COMPLETED'")
    long getTotalStudySecondsByUser(@Param("user") User user);

    /**
     * Demonstrates: Aggregate SUM with date range filter (today / week / month)
     */
    @Query("SELECT COALESCE(SUM(s.actualDurationSeconds), 0) FROM StudySession s " +
           "WHERE s.user = :user AND s.status = 'COMPLETED' AND s.startTime >= :since")
    long getStudySecondsByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Demonstrates DBMS Concepts: GROUP BY, Aggregate SUM and COUNT, ORDER BY
     * Groups completed sessions by subject to compute distribution
     */
    @Query("SELECT COALESCE(s.subject, 'General'), SUM(s.actualDurationSeconds), COUNT(s) " +
           "FROM StudySession s " +
           "WHERE s.user = :user AND s.status = 'COMPLETED' " +
           "GROUP BY s.subject " +
           "ORDER BY SUM(s.actualDurationSeconds) DESC")
    List<Object[]> getSubjectDistributionByUser(@Param("user") User user);

    /**
     * Demonstrates DBMS Concept: GROUP BY with HAVING clause
     * Finds subjects where the user has spent more than a minimum threshold duration
     */
    @Query("SELECT s.subject, SUM(s.actualDurationSeconds) " +
           "FROM StudySession s " +
           "WHERE s.user = :user AND s.status = 'COMPLETED' AND s.subject IS NOT NULL " +
           "GROUP BY s.subject " +
           "HAVING SUM(s.actualDurationSeconds) > :minDurationSeconds")
    List<Object[]> getSignificantSubjectsByUser(@Param("user") User user, @Param("minDurationSeconds") long minDurationSeconds);

    /**
     * Demonstrates DBMS Concept: Nested Subquery
     * Finds sessions with duration greater than the user's average session duration
     */
    @Query("SELECT s FROM StudySession s " +
           "WHERE s.user = :user AND s.status = 'COMPLETED' AND s.actualDurationSeconds > (" +
           "    SELECT AVG(s2.actualDurationSeconds) FROM StudySession s2 " +
           "    WHERE s2.user = :user AND s2.status = 'COMPLETED'" +
           ") ORDER BY s.actualDurationSeconds DESC")
    List<StudySession> findAboveAverageSessionsByUser(@Param("user") User user);
}
