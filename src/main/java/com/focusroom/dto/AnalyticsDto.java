package com.focusroom.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    private double totalStudyHours;
    private double todayStudyHours;
    private double weekStudyHours;
    private double monthStudyHours;
    private long totalSessions;
    private int currentStreakDays;
    private String mostProductiveDay;
    private Map<String, Double> subjectDistribution; // Subject -> Hours
    private List<String> dailyTrendDates;
    private List<Double> dailyTrendHours;

    public AnalyticsDto() {
    }

    public double getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(double totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public double getTodayStudyHours() {
        return todayStudyHours;
    }

    public void setTodayStudyHours(double todayStudyHours) {
        this.todayStudyHours = todayStudyHours;
    }

    public double getWeekStudyHours() {
        return weekStudyHours;
    }

    public void setWeekStudyHours(double weekStudyHours) {
        this.weekStudyHours = weekStudyHours;
    }

    public double getMonthStudyHours() {
        return monthStudyHours;
    }

    public void setMonthStudyHours(double monthStudyHours) {
        this.monthStudyHours = monthStudyHours;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public String getMostProductiveDay() {
        return mostProductiveDay;
    }

    public void setMostProductiveDay(String mostProductiveDay) {
        this.mostProductiveDay = mostProductiveDay;
    }

    public Map<String, Double> getSubjectDistribution() {
        return subjectDistribution;
    }

    public void setSubjectDistribution(Map<String, Double> subjectDistribution) {
        this.subjectDistribution = subjectDistribution;
    }

    public List<String> getDailyTrendDates() {
        return dailyTrendDates;
    }

    public void setDailyTrendDates(List<String> dailyTrendDates) {
        this.dailyTrendDates = dailyTrendDates;
    }

    public List<Double> getDailyTrendHours() {
        return dailyTrendHours;
    }

    public void setDailyTrendHours(List<Double> dailyTrendHours) {
        this.dailyTrendHours = dailyTrendHours;
    }
}
