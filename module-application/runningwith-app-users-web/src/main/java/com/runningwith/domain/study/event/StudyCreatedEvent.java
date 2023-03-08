package com.runningwith.domain.study.event;

import com.runningwith.domain.study.StudyEntity;
import lombok.Getter;

@Getter
public class StudyCreatedEvent {

    private StudyEntity studyEntity;

    public StudyCreatedEvent(StudyEntity studyEntity) {
        this.studyEntity = studyEntity;
    }
}
