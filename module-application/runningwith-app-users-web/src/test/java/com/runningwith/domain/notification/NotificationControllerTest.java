package com.runningwith.domain.notification;

import com.runningwith.domain.notification.factory.NotificationFactory;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.WithUser;
import com.runningwith.infra.MockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.runningwith.domain.notification.NotificationController.*;
import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    NotificationFactory notificationFactory;

    @Autowired
    NotificationService notificationService;

    @WithUser
    @DisplayName("읽지 않은 웹 알림 뷰")
    @Test
    void view_unmarked_notifications() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        NotificationEntity studyCreatedNotification = notificationFactory.createNotificationEntity(usersEntity);

        mockMvc.perform(get(URL_NOTIFICATIONS))
                .andExpect(model().attribute("numberOfNotChecked", Long.valueOf(1)))
                .andExpect(model().attribute("numberOfChecked", Long.valueOf(0)))
                .andExpect(result -> {
                    Map<String, Object> model = result.getModelAndView().getModel();
                    assertThatModelAttribute(studyCreatedNotification, model);
                })
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_NOTIFICATION_LIST));
    }

    @WithUser
    @DisplayName("읽은 웹 알림 뷰")
    @Test
    void view_marked_notifications() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        NotificationEntity studyCreatedNotification = notificationFactory.createNotificationEntity(usersEntity);
        notificationService.markAsRead(List.of(studyCreatedNotification));

        mockMvc.perform(get(URL_NOTIFICATIONS_MARKED))
                .andExpect(model().attribute("numberOfNotChecked", Long.valueOf(0)))
                .andExpect(model().attribute("numberOfChecked", Long.valueOf(1)))
                .andExpect(result -> {
                    Map<String, Object> model = result.getModelAndView().getModel();
                    assertThatModelAttribute(studyCreatedNotification, model);
                })
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_NOTIFICATION_LIST));
    }

    private void assertThatModelAttribute(NotificationEntity studyCreatedNotification, Map<String, Object> model) {
        assertThat(model().attributeExists("notifications"));
        assertThat(model().attributeExists("newStudyNotifications"));
        assertThat(model().attributeExists("eventEnrollmentNotifications"));
        assertThat(model().attributeExists("eventEnrollmentNotifications"));

        List<NotificationEntity> notifications = (List<NotificationEntity>) model.get("notifications");
        List<NotificationEntity> newStudyNotifications = (List<NotificationEntity>) model.get("newStudyNotifications");
        List<NotificationEntity> eventEnrollmentNotifications = (List<NotificationEntity>) model.get("eventEnrollmentNotifications");
        List<NotificationEntity> watchingStudyNotifications = (List<NotificationEntity>) model.get("eventEnrollmentNotifications");

        assertThat(notifications).contains(studyCreatedNotification);
        assertThat(newStudyNotifications).contains(studyCreatedNotification);
        assertThat(eventEnrollmentNotifications).doesNotContain(studyCreatedNotification);
        assertThat(watchingStudyNotifications).doesNotContain(studyCreatedNotification);
    }
}