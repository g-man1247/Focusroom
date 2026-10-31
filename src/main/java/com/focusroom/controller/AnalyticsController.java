package com.focusroom.controller;

import com.focusroom.dto.AnalyticsDto;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.service.StudyAnalyticsService;
import com.focusroom.service.StudySessionService;
import com.focusroom.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AnalyticsController {

    private final StudyAnalyticsService analyticsService;
    private final StudySessionService sessionService;
    private final UserService userService;

    public AnalyticsController(StudyAnalyticsService analyticsService,
                               StudySessionService sessionService,
                               UserService userService) {
        this.analyticsService = analyticsService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @GetMapping("/analytics")
    public String analyticsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        AnalyticsDto analytics = analyticsService.getUserAnalytics(user);
        List<StudySession> history = sessionService.getRecentSessions(user, 20);

        model.addAttribute("analytics", analytics);
        model.addAttribute("history", history);
        return "analytics";
    }
}
