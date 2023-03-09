package com.runningwith.domain.event.event;

import com.runningwith.domain.event.EnrollmentEntity;
import com.runningwith.domain.event.EventEntity;
import com.runningwith.domain.notification.NotificationEntity;
import com.runningwith.domain.notification.NotificationRepository;
import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.infra.config.AppMessages;
import com.runningwith.infra.config.AppProperties;
import com.runningwith.infra.mail.EmailMessage;
import com.runningwith.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

import static com.runningwith.domain.notification.enumeration.NotificationType.EVENT_ENROLLMENT;
import static com.runningwith.infra.utils.CustomStringUtils.getEncodedUrl;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final AppMessages appMessages;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        EnrollmentEntity enrollmentEntity = enrollmentEvent.enrollmentEntity();
        UsersEntity usersEntity = enrollmentEntity.getUsersEntity();
        EventEntity eventEntity = enrollmentEntity.getEventEntity();
        StudyEntity studyEntity = eventEntity.getStudyEntity();

        if (usersEntity.isStudyEnrollmentResultByEmail()) {
            sendEnrollmentNotificationEmail(enrollmentEvent, usersEntity, eventEntity, studyEntity);
        }

        if (usersEntity.isStudyEnrollmentResultByWeb()) {
            sendEnrollmentNotificationWeb(enrollmentEvent, usersEntity, eventEntity, studyEntity);
        }

    }

    private void sendEnrollmentNotificationWeb(EnrollmentEvent enrollmentEvent, UsersEntity usersEntity, EventEntity eventEntity, StudyEntity studyEntity) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title(studyEntity.getTitle() + " / " + eventEntity.getTitle())
                .link("/study/" + getEncodedUrl(studyEntity.getPath()) + "/events/" + eventEntity.getId())
                .checked(false)
                .createdTime(LocalDateTime.now())
                .message(enrollmentEvent.message())
                .usersEntity(usersEntity)
                .notificationType(EVENT_ENROLLMENT)
                .build();

        notificationRepository.save(notificationEntity);
    }

    private void sendEnrollmentNotificationEmail(EnrollmentEvent enrollmentEvent, UsersEntity usersEntity, EventEntity eventEntity, StudyEntity studyEntity) {
        Context context = new Context();
        context.setVariable("nickname", usersEntity.getNickname());
        context.setVariable("link", "/study/" + getEncodedUrl(studyEntity.getPath()) + "/events/" + eventEntity.getId());
        context.setVariable("linkName", studyEntity.getTitle());
        context.setVariable("message", enrollmentEvent.message());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(appMessages.getDomainName() + " " + eventEntity.getTitle() + " 모임 참가 신청 알림")
                .to(usersEntity.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);


    }
}
