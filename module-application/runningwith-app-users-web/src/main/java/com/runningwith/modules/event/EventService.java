package com.runningwith.modules.event;

import com.runningwith.modules.event.form.EventForm;
import com.runningwith.modules.study.StudyEntity;
import com.runningwith.modules.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EventEntity createEvent(EventEntity eventEntity, StudyEntity studyEntity, UsersEntity usersEntity) {
        eventEntity.create(studyEntity, usersEntity);
        return eventRepository.save(eventEntity);
    }


    public void updateEvent(EventEntity eventEntity, EventForm eventForm) {
        eventEntity.update(eventForm.getTitle(), eventForm.getDescription(), eventForm.getEventType(),
                eventForm.getEndEnrollmentDateTime(), eventForm.getStartDateTime(), eventForm.getEndDateTime(), eventForm.getLimitOfEnrollments());
        eventEntity.acceptWaitingList();
    }

    public void deleteEvent(EventEntity eventEntity) {
        eventRepository.delete(eventEntity);
    }

    public void newEnrollment(EventEntity eventEntity, UsersEntity usersEntity) {
        if (!enrollmentRepository.existsByEventEntityAndUsersEntity(eventEntity, usersEntity)) {
            EnrollmentEntity enrollmentEntity = new EnrollmentEntity(usersEntity, LocalDateTime.now(), eventEntity.isAbleToAcceptWaitingEnrollment());
            eventEntity.addNewEnrollment(enrollmentEntity);
            enrollmentRepository.save(enrollmentEntity);
        }
    }

    public void cancelEnrollment(EventEntity eventEntity, UsersEntity usersEntity) {
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, usersEntity);
        EnrollmentEntity enrollmentEntity = getIfExistingEnrollment(optionalEnrollment);
        eventEntity.removeEnrollment(enrollmentEntity);
        enrollmentRepository.delete(enrollmentEntity);
        eventEntity.acceptNextWaitingEnrollment();
    }

    private EnrollmentEntity getIfExistingEnrollment(Optional<EnrollmentEntity> optionalEnrollment) {
        return optionalEnrollment.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다."));
    }


    public void acceptEnrollment(EventEntity eventEntity, EnrollmentEntity enrollmentEntity) {
        eventEntity.accept(enrollmentEntity);
    }

    public void rejectEnrollment(EventEntity eventEntity, EnrollmentEntity enrollmentEntity) {
        eventEntity.reject(enrollmentEntity);
    }

    public void checkInEnrollment(EnrollmentEntity enrollmentEntity) {
        enrollmentEntity.updateAttended(true);
    }

    public void cancelCheckInEnrollment(EnrollmentEntity enrollmentEntity) {
        enrollmentEntity.updateAttended(false);
    }
}
