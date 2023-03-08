package com.runningwith.domain.notification;

import com.runningwith.domain.users.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    long countByUsersEntityAndChecked(UsersEntity usersEntity, boolean checked);
}
