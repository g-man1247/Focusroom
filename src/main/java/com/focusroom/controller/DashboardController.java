package com.focusroom.controller;

import com.focusroom.dto.DashboardStatsDto;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.Task;
import com.focusroom.entity.User;
import com.focusroom.service.StudyAnalyticsService;
import com.focusroom.service.StudyRoomService;
import com.focusroom.service.StudySessionService;
import com.focusroom.service.TaskService;
import com.focusroom.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final StudyAnalyticsService analyticsService;
    private final StudyRoomService roomService;
    private final TaskService taskService;
    private final StudySessionService sessionService;

    public DashboardController(UserService userService,
                               StudyAnalyticsService analyticsService,
                               StudyRoomService roomService,
                               TaskService taskService,
                               StudySessionService sessionService) {
        this.userService = userService;
        this.analyticsService = analyticsService;
        this.roomService = roomService;
        this.taskService = taskService;
        this.sessionService = sessionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        DashboardStatsDto stats = analyticsService.getDashboardStats(user);
        List<StudyRoom> publicRooms = roomService.getPublicActiveRooms();
        List<Task> upcomingTasks = taskService.getUpcomingTasks(user, 3);
        List<StudySession> recentSessions = sessionService.getRecentSessions(user, 5);

        model.addAttribute("stats", stats);
        model.addAttribute("activeRooms", publicRooms);
        model.addAttribute("upcomingTasks", upcomingTasks);
        model.addAttribute("recentSessions", recentSessions);

        return "dashboard";
    }
}
