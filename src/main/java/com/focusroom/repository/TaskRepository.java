package com.focusroom.repository;

import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.TaskPriority;
import com.focusroom.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserOrderByDueDateAscCreatedAtDesc(User user);

    List<Task> findByUserAndStatusOrderByDueDateAsc(User user, TaskStatus status);

    List<Task> findByUserAndPriorityOrderByDueDateAsc(User user, TaskPriority priority);

    Optional<Task> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndStatus(User user, TaskStatus status);

    /**
     * Demonstrates: WHERE with multiple criteria, ORDER BY
     * Upcoming incomplete tasks due soon
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.dueDate <= :deadline ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("user") User user, @Param("deadline") LocalDate deadline);

    /**
     * Demonstrates: GROUP BY on task status with COUNT aggregate
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.user = :user GROUP BY t.status")
    List<Object[]> getTaskStatusDistribution(@Param("user") User user);
}
