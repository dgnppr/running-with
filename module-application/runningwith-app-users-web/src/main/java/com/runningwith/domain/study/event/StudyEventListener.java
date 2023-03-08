package com.runningwith.domain.study.event;

import com.runningwith.domain.notification.NotificationEntity;
import com.runningwith.domain.notification.NotificationRepository;
import com.runningwith.domain.notification.enumeration.NotificationType;
import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.study.StudyRepository;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.predicates.UsersPredicates;
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

import static com.runningwith.infra.utils.CustomStringUtils.getEncodedUrl;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final AppMessages appMessages;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        StudyEntity studyEntity = studyRepository.findStudyEntityWithTagsAndZonesById(studyCreatedEvent.getStudyEntity().getId()).get();

        Iterable<UsersEntity> users = usersRepository.findAll(UsersPredicates.findByTagsAndZones(studyEntity.getTags(), studyEntity.getZones()));

        users.forEach(usersEntity -> {
            if (usersEntity.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(studyEntity, usersEntity);
            }

            if (usersEntity.isStudyCreatedByWeb()) {
                sendStudyCreatedWeb(studyEntity, usersEntity);
            }
        });
    }

    private void sendStudyCreatedWeb(StudyEntity studyEntity, UsersEntity usersEntity) {
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title(studyEntity.getTitle())
                .link("/study/" + getEncodedUrl(studyEntity.getPath()))
                .checked(false)
                .createdTime(LocalDateTime.now())
                .message(studyEntity.getShortDescription())
                .usersEntity(usersEntity)
                .notificationType(NotificationType.STUDY_CREATED)
                .build();

        notificationRepository.save(notificationEntity);
    }

    private void sendStudyCreatedEmail(StudyEntity studyEntity, UsersEntity usersEntity) {
        Context context = new Context();
        context.setVariable("nickname", usersEntity.getNickname());
        context.setVariable("link", "/study/" + getEncodedUrl(studyEntity.getPath()));
        context.setVariable("linkName", studyEntity.getTitle());
        context.setVariable("message", "새로운 스터디가 개설되었어요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(appMessages.getDomainName() + " '" + studyEntity.getTitle() + "' 스터디가 개설되었어요.")
                .to(usersEntity.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
