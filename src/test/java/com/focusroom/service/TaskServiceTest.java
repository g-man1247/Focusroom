package com.focusroom.service;

import com.focusroom.dto.TaskDto;
import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.TaskPriority;
import com.focusroom.entity.enums.TaskStatus;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.repository.NotificationRepository;
import com.focusroom.repository.TaskRepository;
import com.focusroom.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user1;
    private User user2;
    private Task task1;

    @BeforeEach
    void setUp() {
        user1 = new User("user1", "user1@univ.edu", "pass", "User One");
        user1.setId(1L);

        user2 = new User("user2", "user2@univ.edu", "pass", "User Two");
        user2.setId(2L);

        task1 = new Task(user1, "Prepare Slides", "DBMS slides", "DBMS", TaskPriority.HIGH, LocalDate.now().plusDays(2));
        task1.setId(10L);
    }

    @Test
    @DisplayName("Should create task successfully for user")
    void testCreateTask() {
        TaskDto dto = new TaskDto();
        dto.setTitle("Review Chapter 4");
        dto.setSubject("OS");
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setDueDate(LocalDate.now().plusDays(5));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task created = taskService.createTask(dto, user1);

        assertNotNull(created);
        assertEquals("Review Chapter 4", created.getTitle());
        assertEquals(user1, created.getUser());
        assertEquals(TaskStatus.PENDING, created.getStatus());
    }

    @Test
    @DisplayName("Should prevent user2 from modifying or toggling user1's task")
    void testUnauthorizedTaskAccess() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task1));

        assertThrows(UnauthorizedAccessException.class, () -> taskService.toggleTaskStatus(10L, user2));
    }

    @Test
    @DisplayName("Should toggle task status from PENDING to COMPLETED")
    void testToggleTaskStatus() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task toggled = taskService.toggleTaskStatus(10L, user1);

        assertEquals(TaskStatus.COMPLETED, toggled.getStatus());
        assertNotNull(toggled.getCompletedAt());
    }
}
