package com.runningwith.domain.notification;

import com.runningwith.domain.notification.enumeration.NotificationType;
import com.runningwith.domain.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_users")
    private UsersEntity usersEntity;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "notification_type")
    private NotificationType notificationType;

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}