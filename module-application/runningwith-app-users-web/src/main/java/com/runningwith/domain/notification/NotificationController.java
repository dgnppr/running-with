package com.runningwith.domain.notification;

import com.runningwith.domain.users.CurrentUser;
import com.runningwith.domain.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationController {

    public static final String URL_NOTIFICATIONS = "/notifications";
    public static final String VIEW_NOTIFICATION_LIST = "notification/list";
    public static final String URL_NOTIFICATIONS_MARKED = "/notifications/marked";
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping(URL_NOTIFICATIONS)
    public String viewUnMarkedNotifications(@CurrentUser UsersEntity usersEntity, Model model) {

        List<NotificationEntity> notCheckedNotifications = getUnMarkedNotifications(usersEntity);
        long numberOfChecked = notificationRepository.countByUsersEntityAndChecked(usersEntity, true);

        putCategorizedNotifications(model, notCheckedNotifications, numberOfChecked, notCheckedNotifications.size());
        model.addAttribute("isNew", true);

        notificationService.markAsRead(notCheckedNotifications);

        return VIEW_NOTIFICATION_LIST;
    }

    @GetMapping(URL_NOTIFICATIONS_MARKED)
    public String viewMarkedNotifications(@CurrentUser UsersEntity usersEntity, Model model) {
        List<NotificationEntity> checkedNotifications = getMarkedNotifications(usersEntity);
        long numberOfNotChecked = notificationRepository.countByUsersEntityAndChecked(usersEntity, false);

        putCategorizedNotifications(model, checkedNotifications, checkedNotifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);

        return VIEW_NOTIFICATION_LIST;
    }

    private List<NotificationEntity> getMarkedNotifications(UsersEntity usersEntity) {
        List<NotificationEntity> checkedNotifications = notificationRepository.findByUsersEntityAndCheckedOrderByCreatedTimeDesc(usersEntity, true);
        return checkedNotifications;
    }

    private List<NotificationEntity> getUnMarkedNotifications(UsersEntity usersEntity) {
        List<NotificationEntity> notCheckedNotifications = notificationRepository.findByUsersEntityAndCheckedOrderByCreatedTimeDesc(usersEntity, false);
        return notCheckedNotifications;
    }

    private void putCategorizedNotifications(Model model, List<NotificationEntity> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {
        List<NotificationEntity> newStudyNotifications = new ArrayList<>();
        List<NotificationEntity> eventEnrollmentNotifications = new ArrayList<>();
        List<NotificationEntity> watchingStudyNotifications = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED:
                    newStudyNotifications.add(notification);
                    break;
                case EVENT_ENROLLMENT:
                    eventEnrollmentNotifications.add(notification);
                    break;
                case STUDY_UPDATED:
                    watchingStudyNotifications.add(notification);
                    break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }
}
