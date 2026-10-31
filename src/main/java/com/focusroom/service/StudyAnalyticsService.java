package com.focusroom.service;

import com.focusroom.dto.AnalyticsDto;
import com.focusroom.dto.DashboardStatsDto;
import com.focusroom.entity.User;

public interface StudyAnalyticsService {

    DashboardStatsDto getDashboardStats(User user);

    AnalyticsDto getUserAnalytics(User user);
}
