package com.focusroom.service.impl;

import com.focusroom.entity.Notification;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.NotificationType;
import com.focusroom.exception.ResourceNotFoundException;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.repository.NotificationRepository;
import com.focusroom.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(User user, String title, String message, NotificationType type, String linkUrl) {
        Notification notification = new Notification(user, title, message, type, linkUrl);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().equals(user)) {
            throw new UnauthorizedAccessException("Cannot access notifications belonging to another user");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
    }
}
