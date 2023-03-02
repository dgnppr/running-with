package com.runningwith.event;

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
}
