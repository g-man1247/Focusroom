package com.focusroom.service.impl;

import com.focusroom.dto.AnalyticsDto;
import com.focusroom.dto.DashboardStatsDto;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.RoomStatus;
import com.focusroom.entity.enums.SessionStatus;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.repository.StudySessionRepository;
import com.focusroom.repository.TaskRepository;
import com.focusroom.service.StudyAnalyticsService;
import com.focusroom.service.StudySessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudyAnalyticsServiceImpl implements StudyAnalyticsService {

    private final StudySessionRepository sessionRepository;
    private final StudyRoomRepository roomRepository;
    private final TaskRepository taskRepository;
    private final StudySessionService sessionService;

    public StudyAnalyticsServiceImpl(StudySessionRepository sessionRepository,
                                     StudyRoomRepository roomRepository,
                                     TaskRepository taskRepository,
                                     StudySessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.roomRepository = roomRepository;
        this.taskRepository = taskRepository;
        this.sessionService = sessionService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(User user) {
        DashboardStatsDto dto = new DashboardStatsDto();

        // Welcome message based on time of day
        int hour = java.time.LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : (hour < 17 ? "Good afternoon" : "Good evening");
        dto.setWelcomeMessage(greeting + ", " + (user.getFullName() != null ? user.getFullName() : user.getUsername()) + "!");

        // Total hours and today's minutes
        long totalSeconds = sessionService.getTotalStudySeconds(user);
        dto.setTotalStudyHours(roundHours(totalSeconds));

        long todaySeconds = sessionService.getTodayStudySeconds(user);
        dto.setTodayStudyMinutes(todaySeconds / 60);

        dto.setCompletedSessions(sessionService.getCompletedSessionsCount(user));
        dto.setCurrentStreakDays(sessionService.calculateCurrentStreakDays(user));
        dto.setActiveRoomsCount((int) roomRepository.countByStatus(RoomStatus.ACTIVE));
        dto.setPendingTasksCount(taskRepository.countByUser(user) - taskRepository.countByUserAndStatus(user, com.focusroom.entity.enums.TaskStatus.COMPLETED));

        // Weekly chart data (last 7 days: Sun to Sat or past 7 days)
        List<String> weeklyLabels = new ArrayList<>();
        List<Double> weeklyHours = new ArrayList<>();

        List<StudySession> completed = sessionRepository.findByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.COMPLETED);
        Map<LocalDate, Long> secondsPerDate = new HashMap<>();
        for (StudySession s : completed) {
            LocalDate d = s.getStartTime().toLocalDate();
            secondsPerDate.put(d, secondsPerDate.getOrDefault(d, 0L) + s.getActualDurationSeconds());
        }

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weeklyLabels.add(date.format(dayFormatter));
            long sec = secondsPerDate.getOrDefault(date, 0L);
            weeklyHours.add(roundHours(sec));
        }

        dto.setWeeklyLabels(weeklyLabels);
        dto.setWeeklyHours(weeklyHours);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDto getUserAnalytics(User user) {
        AnalyticsDto dto = new AnalyticsDto();

        long totalSeconds = sessionService.getTotalStudySeconds(user);
        long todaySeconds = sessionService.getTodayStudySeconds(user);
        long weekSeconds = sessionService.getThisWeekStudySeconds(user);
        long monthSeconds = sessionService.getThisMonthStudySeconds(user);

        dto.setTotalStudyHours(roundHours(totalSeconds));
        dto.setTodayStudyHours(roundHours(todaySeconds));
        dto.setWeekStudyHours(roundHours(weekSeconds));
        dto.setMonthStudyHours(roundHours(monthSeconds));
        dto.setTotalSessions(sessionService.getCompletedSessionsCount(user));
        dto.setCurrentStreakDays(sessionService.calculateCurrentStreakDays(user));

        // Subject Distribution using DBMS GROUP BY query
        List<Object[]> subjectData = sessionRepository.getSubjectDistributionByUser(user);
        Map<String, Double> distribution = new LinkedHashMap<>();
        for (Object[] row : subjectData) {
            String subject = (String) row[0];
            Long sec = (Long) row[1];
            distribution.put(subject, roundHours(sec));
        }
        dto.setSubjectDistribution(distribution);

        // Daily trend (last 14 days) and Most productive day calculation
        List<String> trendDates = new ArrayList<>();
        List<Double> trendHours = new ArrayList<>();

        List<StudySession> completed = sessionRepository.findByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.COMPLETED);
        Map<LocalDate, Long> dateMap = new HashMap<>();
        Map<DayOfWeek, Long> dayOfWeekMap = new EnumMap<>(DayOfWeek.class);

        for (StudySession s : completed) {
            LocalDate d = s.getStartTime().toLocalDate();
            dateMap.put(d, dateMap.getOrDefault(d, 0L) + s.getActualDurationSeconds());
            DayOfWeek dow = d.getDayOfWeek();
            dayOfWeekMap.put(dow, dayOfWeekMap.getOrDefault(dow, 0L) + s.getActualDurationSeconds());
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            trendDates.add(d.format(dateFmt));
            trendHours.add(roundHours(dateMap.getOrDefault(d, 0L)));
        }

        dto.setDailyTrendDates(trendDates);
        dto.setDailyTrendHours(trendHours);

        // Find most productive day
        DayOfWeek bestDay = null;
        long maxSeconds = -1;
        for (Map.Entry<DayOfWeek, Long> entry : dayOfWeekMap.entrySet()) {
            if (entry.getValue() > maxSeconds) {
                maxSeconds = entry.getValue();
                bestDay = entry.getKey();
            }
        }
        dto.setMostProductiveDay(bestDay != null ? bestDay.name().substring(0, 1) + bestDay.name().substring(1).toLowerCase() : "N/A");

        return dto;
    }

    private double roundHours(long seconds) {
        if (seconds <= 0) return 0.0;
        double hours = (double) seconds / 3600.0;
        return BigDecimal.valueOf(hours).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
