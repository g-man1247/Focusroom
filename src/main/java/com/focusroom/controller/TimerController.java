package com.focusroom.controller;

import com.focusroom.dto.TimerActionRequest;
import com.focusroom.dto.TimerStateResponse;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.service.StudySessionService;
import com.focusroom.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TimerController {

    private final StudySessionService sessionService;
    private final UserService userService;

    public TimerController(StudySessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @GetMapping("/timer")
    public String timerPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        TimerStateResponse timerState = sessionService.getCurrentTimerState(user);
        List<StudySession> recentSessions = sessionService.getRecentSessions(user, 10);

        model.addAttribute("timerState", timerState);
        model.addAttribute("recentSessions", recentSessions);
        return "timer";
    }

    @PostMapping("/api/timer/action")
    @ResponseBody
    public ResponseEntity<TimerStateResponse> timerAction(@RequestBody TimerActionRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        TimerStateResponse response = sessionService.handleTimerAction(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/timer/state")
    @ResponseBody
    public ResponseEntity<TimerStateResponse> getTimerState(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        TimerStateResponse response = sessionService.getCurrentTimerState(user);
        return ResponseEntity.ok(response);
    }
}
