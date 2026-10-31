package com.focusroom.service;

import com.focusroom.dto.TimerActionRequest;
import com.focusroom.dto.TimerStateResponse;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.SessionStatus;
import com.focusroom.entity.enums.SessionType;
import com.focusroom.repository.NotificationRepository;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.repository.StudySessionRepository;
import com.focusroom.service.impl.StudySessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository sessionRepository;

    @Mock
    private StudyRoomRepository roomRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private StudySessionServiceImpl sessionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("sarah", "sarah@univ.edu", "pass", "Sarah Connor");
        user.setId(5L);
    }

    @Test
    @DisplayName("Should start new focus session when none active")
    void testStartNewSession() {
        when(sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.PAUSED))
                .thenReturn(Optional.empty());
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(inv -> inv.getArgument(0));

        TimerActionRequest req = new TimerActionRequest();
        req.setAction("START");
        req.setPlannedMinutes(25);
        req.setSessionType(SessionType.FOCUS);
        req.setSubject("Operating Systems");

        TimerStateResponse response = sessionService.handleTimerAction(req, user);

        assertNotNull(response);
        assertEquals(SessionStatus.IN_PROGRESS, response.getStatus());
        assertTrue(response.isRunning());
        assertEquals(25, response.getPlannedMinutes());
    }

    @Test
    @DisplayName("Should pause in-progress session and record paused state")
    void testPauseActiveSession() {
        StudySession active = new StudySession(user, null, SessionType.FOCUS, 25, "Java");
        active.setId(101L);

        when(sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(active));
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(inv -> inv.getArgument(0));

        TimerActionRequest req = new TimerActionRequest();
        req.setAction("PAUSE");

        TimerStateResponse response = sessionService.handleTimerAction(req, user);

        assertNotNull(response);
        assertEquals(SessionStatus.PAUSED, response.getStatus());
        assertFalse(response.isRunning());
    }

    @Test
    @DisplayName("Should complete active session and persist accurate study duration")
    void testCompleteSession() {
        StudySession active = new StudySession(user, null, SessionType.FOCUS, 25, "Java");
        active.setId(102L);
        active.setStartTime(LocalDateTime.now().minusMinutes(25)); // 25 mins ago

        when(sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(active));
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(inv -> inv.getArgument(0));

        TimerActionRequest req = new TimerActionRequest();
        req.setAction("COMPLETE");

        TimerStateResponse response = sessionService.handleTimerAction(req, user);

        assertNotNull(response);
        assertEquals(SessionStatus.COMPLETED, response.getStatus());
        assertFalse(response.isRunning());
        assertTrue(active.getActualDurationSeconds() > 0);
        verify(notificationRepository, times(1)).save(any());
    }
}
