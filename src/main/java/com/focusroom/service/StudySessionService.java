package com.focusroom.service;

import com.focusroom.dto.TimerActionRequest;
import com.focusroom.dto.TimerStateResponse;
import com.focusroom.entity.StudySession;
import com.focusroom.entity.User;

import java.util.List;
import java.util.Optional;

public interface StudySessionService {

    TimerStateResponse handleTimerAction(TimerActionRequest request, User user);

    TimerStateResponse getCurrentTimerState(User user);

    Optional<StudySession> getActiveSession(User user);

    long getTotalStudySeconds(User user);

    long getTodayStudySeconds(User user);

    long getThisWeekStudySeconds(User user);

    long getThisMonthStudySeconds(User user);

    long getCompletedSessionsCount(User user);

    int calculateCurrentStreakDays(User user);

    List<StudySession> getRecentSessions(User user, int limit);

    long countActiveSessionsAcrossPlatform();
}
