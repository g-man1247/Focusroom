package com.focusroom.service;

import com.focusroom.dto.TaskDto;
import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.TaskPriority;
import com.focusroom.entity.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    List<Task> getTasksForUser(User user, TaskStatus statusFilter, TaskPriority priorityFilter);

    Task getTaskByIdAndUser(Long taskId, User user);

    Task createTask(TaskDto dto, User user);

    Task updateTask(Long taskId, TaskDto dto, User user);

    Task toggleTaskStatus(Long taskId, User user);

    void deleteTask(Long taskId, User user);

    List<Task> getUpcomingTasks(User user, int daysAhead);

    long countPendingTasks(User user);

    long countTotalTasks(User user);
}
