package com.runningwith.domain.event.event;

import com.runningwith.domain.event.EnrollmentEntity;

public record EnrollmentEvent(EnrollmentEntity enrollmentEntity, String message) {
}
