package com.focusroom.service.impl;

import com.focusroom.dto.TaskDto;
import com.focusroom.entity.Notification;
import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.NotificationType;
import com.focusroom.entity.enums.TaskPriority;
import com.focusroom.entity.enums.TaskStatus;
import com.focusroom.exception.ResourceNotFoundException;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.repository.NotificationRepository;
import com.focusroom.repository.TaskRepository;
import com.focusroom.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    public TaskServiceImpl(TaskRepository taskRepository, NotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksForUser(User user, TaskStatus statusFilter, TaskPriority priorityFilter) {
        List<Task> tasks = taskRepository.findByUserOrderByDueDateAscCreatedAtDesc(user);

        return tasks.stream()
                .filter(t -> statusFilter == null || t.getStatus() == statusFilter)
                .filter(t -> priorityFilter == null || t.getPriority() == priorityFilter)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskByIdAndUser(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().equals(user)) {
            throw new UnauthorizedAccessException("You are not authorized to view this task");
        }

        return task;
    }

    @Override
    @Transactional
    public Task createTask(TaskDto dto, User user) {
        Task task = new Task();
        task.setUser(user);
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        task.setSubject(dto.getSubject() != null ? dto.getSubject().trim() : null);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM);
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setDueDate(dto.getDueDate());

        Task savedTask = taskRepository.save(task);

        // If task due today or tomorrow, send an alert notification
        if (savedTask.getDueDate() != null && !savedTask.getDueDate().isAfter(LocalDate.now().plusDays(1))) {
            Notification alert = new Notification(
                    user,
                    "Task Due Soon: " + savedTask.getTitle(),
                    "Your task is due on " + savedTask.getDueDate() + ". Stay on track!",
                    NotificationType.DEADLINE_ALERT,
                    "/tasks"
            );
            notificationRepository.save(alert);
        }

        return savedTask;
    }

    @Override
    @Transactional
    public Task updateTask(Long taskId, TaskDto dto, User user) {
        Task task = getTaskByIdAndUser(taskId, user);

        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        task.setSubject(dto.getSubject() != null ? dto.getSubject().trim() : null);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : task.getPriority());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : task.getStatus());
        task.setDueDate(dto.getDueDate());

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task toggleTaskStatus(Long taskId, User user) {
        Task task = getTaskByIdAndUser(taskId, user);

        if (task.isCompleted()) {
            task.markPending();
        } else {
            task.markCompleted();
        }

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, User user) {
        Task task = getTaskByIdAndUser(taskId, user);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getUpcomingTasks(User user, int daysAhead) {
        LocalDate deadline = LocalDate.now().plusDays(daysAhead);
        return taskRepository.findUpcomingTasks(user, deadline);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingTasks(User user) {
        return taskRepository.countByUserAndStatus(user, TaskStatus.PENDING) +
               taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalTasks(User user) {
        return taskRepository.countByUser(user);
    }
}
