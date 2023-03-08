package com.runningwith.domain.study.event;

import com.runningwith.domain.study.StudyEntity;

public record StudyUpdatedEvent(StudyEntity studyEntity, String message) {
}
