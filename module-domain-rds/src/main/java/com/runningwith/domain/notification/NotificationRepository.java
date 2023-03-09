package com.runningwith.domain.notification;

import com.runningwith.domain.users.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    long countByUsersEntityAndChecked(UsersEntity usersEntity, boolean checked);

    List<NotificationEntity> findByUsersEntityAndCheckedOrderByCreatedTimeDesc(UsersEntity usersEntity, boolean checked);
}
