package com.focusroom.dto;

import java.util.List;

public class DashboardStatsDto {

    private String welcomeMessage;
    private double totalStudyHours;
    private long todayStudyMinutes;
    private long completedSessions;
    private int currentStreakDays;
    private int activeRoomsCount;
    private long pendingTasksCount;
    private List<String> weeklyLabels;
    private List<Double> weeklyHours;

    public DashboardStatsDto() {
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public double getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(double totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public long getTodayStudyMinutes() {
        return todayStudyMinutes;
    }

    public void setTodayStudyMinutes(long todayStudyMinutes) {
        this.todayStudyMinutes = todayStudyMinutes;
    }

    public long getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(long completedSessions) {
        this.completedSessions = completedSessions;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public int getActiveRoomsCount() {
        return activeRoomsCount;
    }

    public void setActiveRoomsCount(int activeRoomsCount) {
        this.activeRoomsCount = activeRoomsCount;
    }

    public long getPendingTasksCount() {
        return pendingTasksCount;
    }

    public void setPendingTasksCount(long pendingTasksCount) {
        this.pendingTasksCount = pendingTasksCount;
    }

    public List<String> getWeeklyLabels() {
        return weeklyLabels;
    }

    public void setWeeklyLabels(List<String> weeklyLabels) {
        this.weeklyLabels = weeklyLabels;
    }

    public List<Double> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(List<Double> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }
}
