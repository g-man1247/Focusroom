package com.focusroom.dto;

import com.focusroom.entity.enums.SessionStatus;
import com.focusroom.entity.enums.SessionType;

import java.time.LocalDateTime;

public class TimerStateResponse {

    private Long sessionId;
    private SessionStatus status;
    private SessionType sessionType;
    private int plannedMinutes;
    private long activeSeconds;
    private long pausedSeconds;
    private long remainingSeconds;
    private boolean running;
    private LocalDateTime startTime;
    private String subject;

    public TimerStateResponse() {
    }

    public TimerStateResponse(Long sessionId, SessionStatus status, SessionType sessionType, 
                              int plannedMinutes, long activeSeconds, long pausedSeconds, 
                              long remainingSeconds, boolean running, LocalDateTime startTime, String subject) {
        this.sessionId = sessionId;
        this.status = status;
        this.sessionType = sessionType;
        this.plannedMinutes = plannedMinutes;
        this.activeSeconds = activeSeconds;
        this.pausedSeconds = pausedSeconds;
        this.remainingSeconds = remainingSeconds;
        this.running = running;
        this.startTime = startTime;
        this.subject = subject;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public void setPlannedMinutes(int plannedMinutes) {
        this.plannedMinutes = plannedMinutes;
    }

    public long getActiveSeconds() {
        return activeSeconds;
    }

    public void setActiveSeconds(long activeSeconds) {
        this.activeSeconds = activeSeconds;
    }

    public long getPausedSeconds() {
        return pausedSeconds;
    }

    public void setPausedSeconds(long pausedSeconds) {
        this.pausedSeconds = pausedSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
