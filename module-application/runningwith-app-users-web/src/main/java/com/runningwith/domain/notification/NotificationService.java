package com.runningwith.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAsRead(List<NotificationEntity> notifications) {
        notifications.forEach(notificationEntity -> notificationEntity.updateChecked(true));
        notificationRepository.saveAll(notifications);
    }
}
