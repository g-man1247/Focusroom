package com.focusroom.entity;

import com.focusroom.entity.enums.SessionStatus;
import com.focusroom.entity.enums.SessionType;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "study_sessions",
    indexes = {
        @Index(name = "idx_session_user", columnList = "user_id"),
        @Index(name = "idx_session_room", columnList = "room_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_start_time", columnList = "start_time"),
        @Index(name = "idx_session_user_status", columnList = "user_id, status")
    }
)
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", foreignKey = @ForeignKey(name = "fk_session_room"))
    private StudyRoom room;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    private SessionType sessionType = SessionType.FOCUS;

    @Column(name = "planned_duration_minutes", nullable = false)
    private int plannedDurationMinutes = 25;

    @Column(name = "actual_duration_seconds", nullable = false)
    private long actualDurationSeconds = 0;

    @Column(name = "paused_duration_seconds", nullable = false)
    private long pausedDurationSeconds = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "last_pause_time")
    private LocalDateTime lastPauseTime;

    @Column(length = 50)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public StudySession() {
    }

    public StudySession(User user, StudyRoom room, SessionType sessionType, int plannedDurationMinutes, String subject) {
        this.user = user;
        this.room = room;
        this.sessionType = sessionType != null ? sessionType : SessionType.FOCUS;
        this.plannedDurationMinutes = plannedDurationMinutes > 0 ? plannedDurationMinutes : 25;
        this.subject = subject;
        this.status = SessionStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.actualDurationSeconds = 0;
        this.pausedDurationSeconds = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }

    /**
     * Pauses an in-progress session.
     */
    public void pauseSession() {
        if (this.status == SessionStatus.IN_PROGRESS) {
            this.status = SessionStatus.PAUSED;
            this.lastPauseTime = LocalDateTime.now();
        }
    }

    /**
     * Resumes a paused session and accumulates paused time so it is NOT counted as study time.
     */
    public void resumeSession() {
        if (this.status == SessionStatus.PAUSED) {
            if (this.lastPauseTime != null) {
                long pausedSegment = Duration.between(this.lastPauseTime, LocalDateTime.now()).getSeconds();
                this.pausedDurationSeconds += Math.max(0, pausedSegment);
                this.lastPauseTime = null;
            }
            this.status = SessionStatus.IN_PROGRESS;
        }
    }

    /**
     * Completes the session, accurately computing actual active duration.
     */
    public void completeSession() {
        this.endTime = LocalDateTime.now();
        if (this.status == SessionStatus.PAUSED && this.lastPauseTime != null) {
            long finalPaused = Duration.between(this.lastPauseTime, this.endTime).getSeconds();
            this.pausedDurationSeconds += Math.max(0, finalPaused);
            this.lastPauseTime = null;
        }
        long totalElapsedSeconds = Duration.between(this.startTime, this.endTime).getSeconds();
        this.actualDurationSeconds = Math.max(0, totalElapsedSeconds - this.pausedDurationSeconds);
        this.status = SessionStatus.COMPLETED;
    }

    /**
     * Cancels the session.
     */
    public void cancelSession() {
        this.endTime = LocalDateTime.now();
        if (this.status == SessionStatus.PAUSED && this.lastPauseTime != null) {
            long finalPaused = Duration.between(this.lastPauseTime, this.endTime).getSeconds();
            this.pausedDurationSeconds += Math.max(0, finalPaused);
            this.lastPauseTime = null;
        }
        long totalElapsedSeconds = Duration.between(this.startTime, this.endTime).getSeconds();
        this.actualDurationSeconds = Math.max(0, totalElapsedSeconds - this.pausedDurationSeconds);
        this.status = SessionStatus.CANCELLED;
    }

    /**
     * Calculates the currently elapsed active study seconds up to this moment.
     */
    public long getCurrentActiveSeconds() {
        if (this.status == SessionStatus.COMPLETED || this.status == SessionStatus.CANCELLED) {
            return this.actualDurationSeconds;
        }
        LocalDateTime now = LocalDateTime.now();
        long totalElapsed = Duration.between(this.startTime, now).getSeconds();
        long currentPaused = this.pausedDurationSeconds;
        if (this.status == SessionStatus.PAUSED && this.lastPauseTime != null) {
            currentPaused += Duration.between(this.lastPauseTime, now).getSeconds();
        }
        return Math.max(0, totalElapsed - currentPaused);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StudyRoom getRoom() {
        return room;
    }

    public void setRoom(StudyRoom room) {
        this.room = room;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public int getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }

    public void setPlannedDurationMinutes(int plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }

    public long getActualDurationSeconds() {
        return actualDurationSeconds;
    }

    public void setActualDurationSeconds(long actualDurationSeconds) {
        this.actualDurationSeconds = actualDurationSeconds;
    }

    public long getPausedDurationSeconds() {
        return pausedDurationSeconds;
    }

    public void setPausedDurationSeconds(long pausedDurationSeconds) {
        this.pausedDurationSeconds = pausedDurationSeconds;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastPauseTime() {
        return lastPauseTime;
    }

    public void setLastPauseTime(LocalDateTime lastPauseTime) {
        this.lastPauseTime = lastPauseTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudySession that = (StudySession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
