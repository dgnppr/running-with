package com.runningwith.domain.notification.factory;

import com.runningwith.domain.notification.NotificationEntity;
import com.runningwith.domain.notification.NotificationRepository;
import com.runningwith.domain.notification.enumeration.NotificationType;
import com.runningwith.domain.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final NotificationRepository notificationRepository;

    public NotificationEntity createNotificationEntity(UsersEntity usersEntity) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title("test title")
                .link("test link")
                .checked(false)
                .notificationType(NotificationType.STUDY_CREATED)
                .createdTime(LocalDateTime.now())
                .message("test _message")
                .usersEntity(usersEntity)
                .build();

        return notificationRepository.save(notificationEntity);
    }
}
