package com.focusroom.service.impl;

import com.focusroom.dto.TimerActionRequest;
import com.focusroom.dto.TimerStateResponse;
import com.focusroom.entity.Notification;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.NotificationType;
import com.focusroom.entity.enums.SessionStatus;
import com.focusroom.entity.enums.SessionType;
import com.focusroom.exception.InvalidOperationException;
import com.focusroom.repository.NotificationRepository;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.repository.StudySessionRepository;
import com.focusroom.service.StudySessionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final StudyRoomRepository roomRepository;
    private final NotificationRepository notificationRepository;

    public StudySessionServiceImpl(StudySessionRepository sessionRepository,
                                   StudyRoomRepository roomRepository,
                                   NotificationRepository notificationRepository) {
        this.sessionRepository = sessionRepository;
        this.roomRepository = roomRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public TimerStateResponse handleTimerAction(TimerActionRequest request, User user) {
        String action = request.getAction() != null ? request.getAction().toUpperCase() : "GET";
        Optional<StudySession> activeOpt = getActiveSession(user);

        StudySession session;

        switch (action) {
            case "START":
                // If an active session is already ongoing, pause or cancel it
                if (activeOpt.isPresent()) {
                    session = activeOpt.get();
                    if (session.getStatus() == SessionStatus.PAUSED) {
                        session.resumeSession();
                        sessionRepository.save(session);
                        return buildTimerResponse(session);
                    }
                }
                
                StudyRoom room = null;
                if (request.getRoomId() != null) {
                    room = roomRepository.findById(request.getRoomId()).orElse(null);
                }

                int plannedMinutes = request.getPlannedMinutes() > 0 ? request.getPlannedMinutes() : 25;
                SessionType type = request.getSessionType() != null ? request.getSessionType() : SessionType.FOCUS;
                String subject = request.getSubject() != null && !request.getSubject().isBlank() ? request.getSubject().trim() : "General Study";

                session = new StudySession(user, room, type, plannedMinutes, subject);
                session = sessionRepository.save(session);
                break;

            case "PAUSE":
                if (activeOpt.isEmpty()) {
                    throw new InvalidOperationException("No active session to pause");
                }
                session = activeOpt.get();
                session.pauseSession();
                session = sessionRepository.save(session);
                break;

            case "RESUME":
                if (activeOpt.isEmpty()) {
                    throw new InvalidOperationException("No paused session to resume");
                }
                session = activeOpt.get();
                session.resumeSession();
                session = sessionRepository.save(session);
                break;

            case "COMPLETE":
                if (activeOpt.isEmpty()) {
                    throw new InvalidOperationException("No active session to complete");
                }
                session = activeOpt.get();
                session.completeSession();
                session = sessionRepository.save(session);

                // Send milestone / completion notification
                long minutes = session.getActualDurationSeconds() / 60;
                Notification notification = new Notification(
                        user,
                        "Focus Session Completed!",
                        "Great job! You logged " + minutes + " minutes of active study on " + session.getSubject() + ".",
                        NotificationType.SESSION_COMPLETED,
                        "/analytics"
                );
                notificationRepository.save(notification);
                break;

            case "CANCEL":
                if (activeOpt.isPresent()) {
                    session = activeOpt.get();
                    session.cancelSession();
                    session = sessionRepository.save(session);
                } else {
                    return new TimerStateResponse(null, SessionStatus.CANCELLED, SessionType.FOCUS, 25, 0, 0, 25 * 60, false, null, null);
                }
                break;

            default:
                // Just query current state
                if (activeOpt.isPresent()) {
                    return buildTimerResponse(activeOpt.get());
                } else {
                    return new TimerStateResponse(null, SessionStatus.COMPLETED, SessionType.FOCUS, 25, 0, 0, 25 * 60, false, null, null);
                }
        }

        return buildTimerResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public TimerStateResponse getCurrentTimerState(User user) {
        Optional<StudySession> activeOpt = getActiveSession(user);
        return activeOpt.map(this::buildTimerResponse)
                .orElseGet(() -> new TimerStateResponse(null, SessionStatus.COMPLETED, SessionType.FOCUS, 25, 0, 0, 25 * 60, false, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudySession> getActiveSession(User user) {
        Optional<StudySession> inProgress = sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS);
        if (inProgress.isPresent()) {
            return inProgress;
        }
        return sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.PAUSED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalStudySeconds(User user) {
        return sessionRepository.getTotalStudySecondsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTodayStudySeconds(User user) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return sessionRepository.getStudySecondsByUserSince(user, startOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public long getThisWeekStudySeconds(User user) {
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), LocalTime.MIN);
        return sessionRepository.getStudySecondsByUserSince(user, startOfWeek);
    }

    @Override
    @Transactional(readOnly = true)
    public long getThisMonthStudySeconds(User user) {
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
        return sessionRepository.getStudySecondsByUserSince(user, startOfMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCompletedSessionsCount(User user) {
        return sessionRepository.countByUserAndStatus(user, SessionStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateCurrentStreakDays(User user) {
        List<StudySession> completedSessions = sessionRepository.findByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.COMPLETED);
        if (completedSessions.isEmpty()) {
            return 0;
        }

        Set<LocalDate> studyDates = new TreeSet<>(Collections.reverseOrder());
        for (StudySession s : completedSessions) {
            studyDates.add(s.getStartTime().toLocalDate());
        }

        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        // If no study today, check if streak is alive from yesterday
        if (!studyDates.contains(today)) {
            checkDate = today.minusDays(1);
            if (!studyDates.contains(checkDate)) {
                return 0;
            }
        }

        int streak = 0;
        while (studyDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySession> getRecentSessions(User user, int limit) {
        return sessionRepository.findByUserOrderByStartTimeDesc(user, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveSessionsAcrossPlatform() {
        return sessionRepository.countByStatus(SessionStatus.IN_PROGRESS) +
               sessionRepository.countByStatus(SessionStatus.PAUSED);
    }

    private TimerStateResponse buildTimerResponse(StudySession session) {
        long activeSeconds = session.getCurrentActiveSeconds();
        long plannedTotalSeconds = session.getPlannedDurationMinutes() * 60L;
        long remaining = Math.max(0, plannedTotalSeconds - activeSeconds);
        boolean running = session.getStatus() == SessionStatus.IN_PROGRESS;

        return new TimerStateResponse(
                session.getId(),
                session.getStatus(),
                session.getSessionType(),
                session.getPlannedDurationMinutes(),
                activeSeconds,
                session.getPausedDurationSeconds(),
                remaining,
                running,
                session.getStartTime(),
                session.getSubject()
        );
    }
}
