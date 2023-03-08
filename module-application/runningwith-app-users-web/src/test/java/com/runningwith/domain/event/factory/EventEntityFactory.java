package com.runningwith.domain.event.factory;

import com.runningwith.domain.event.EventEntity;
import com.runningwith.domain.event.EventRepository;
import com.runningwith.domain.event.EventService;
import com.runningwith.domain.event.enumeration.EventType;
import com.runningwith.domain.event.form.EventForm;
import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventEntityFactory {

    @Autowired
    EventService eventService;

    @Autowired
    EventRepository eventRepository;

    public EventEntity createEvent(EventEntity eventEntity, StudyEntity studyEntity, UsersEntity usersEntity) {
        return eventService.createEvent(eventEntity, studyEntity, usersEntity);
    }

    public EventForm createEventForm(String eventFrom_description, String eventFrom_title, int limitOfEnrollments, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime, EventType eventType) {
        EventForm eventForm = new EventForm();
        eventForm.setEventType(eventType);
        eventForm.setDescription(eventFrom_description);
        eventForm.setTitle(eventFrom_title);
        eventForm.setLimitOfEnrollments(limitOfEnrollments);
        eventForm.setEndEnrollmentDateTime(endEnrollmentDateTime);
        eventForm.setStartDateTime(startDateTime);
        eventForm.setEndDateTime(endDateTime);
        return eventForm;
    }
}
