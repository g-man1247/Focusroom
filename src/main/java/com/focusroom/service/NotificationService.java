package com.focusroom.service;

import com.focusroom.entity.Notification;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    Notification createNotification(User user, String title, String message, NotificationType type, String linkUrl);

    List<Notification> getUserNotifications(User user);

    long getUnreadCount(User user);

    void markAsRead(Long notificationId, User user);

    void markAllAsRead(User user);
}
