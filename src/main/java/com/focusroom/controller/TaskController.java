package com.focusroom.controller;

import com.focusroom.dto.TaskDto;
import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.TaskPriority;
import com.focusroom.entity.enums.TaskStatus;
import com.focusroom.service.TaskService;
import com.focusroom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping
    public String taskManager(@RequestParam(value = "status", required = false) TaskStatus statusFilter,
                              @RequestParam(value = "priority", required = false) TaskPriority priorityFilter,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Task> tasks = taskService.getTasksForUser(user, statusFilter, priorityFilter);

        model.addAttribute("tasks", tasks);
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("selectedPriority", priorityFilter);
        model.addAttribute("newTask", new TaskDto());
        model.addAttribute("pendingCount", taskService.countPendingTasks(user));
        model.addAttribute("totalCount", taskService.countTotalTasks(user));

        return "tasks";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("newTask") TaskDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid task title (2-150 characters).");
            return "redirect:/tasks";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.createTask(dto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Task created successfully!");
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/edit")
    public String editTask(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("task") TaskDto dto,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid task data.");
            return "redirect:/tasks";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.updateTask(id, dto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully!");
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/toggle")
    public String toggleTask(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Task updated = taskService.toggleTaskStatus(id, user);
        String msg = updated.isCompleted() ? "Task marked as completed! Nice work!" : "Task marked as pending.";
        redirectAttributes.addFlashAttribute("infoMessage", msg);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.deleteTask(id, user);
        redirectAttributes.addFlashAttribute("infoMessage", "Task removed.");
        return "redirect:/tasks";
    }
}
