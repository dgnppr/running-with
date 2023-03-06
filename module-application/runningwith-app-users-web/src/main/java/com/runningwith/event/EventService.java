package com.runningwith.event;

import com.runningwith.event.form.EventForm;
import com.runningwith.study.StudyEntity;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventEntity createEvent(EventEntity eventEntity, StudyEntity studyEntity, UsersEntity usersEntity) {
        eventEntity.create(studyEntity, usersEntity);
        return eventRepository.save(eventEntity);
    }

    // TODO 모집인원 늘릴 경우, 선착순 모임의 경우에 , 자동으로 추가하는 로직 추가
    public void updateEvent(EventEntity eventEntity, EventForm eventForm) {
        eventEntity.update(eventForm.getTitle(), eventForm.getDescription(), eventForm.getEventType(),
                eventForm.getEndEnrollmentDateTime(), eventForm.getStartDateTime(), eventForm.getEndDateTime(), eventForm.getLimitOfEnrollments());
    }

    public void deleteEvent(EventEntity eventEntity) {
        eventRepository.delete(eventEntity);
    }
}
