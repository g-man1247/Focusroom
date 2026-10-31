package com.focusroom.dto;

import com.focusroom.entity.enums.SessionType;

public class TimerActionRequest {

    private String action; // START, PAUSE, RESUME, COMPLETE, CANCEL
    private SessionType sessionType = SessionType.FOCUS;
    private int plannedMinutes = 25;
    private String subject;
    private Long roomId;

    public TimerActionRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
