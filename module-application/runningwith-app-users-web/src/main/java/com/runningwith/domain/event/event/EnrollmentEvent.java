package com.runningwith.domain.event.event;


import com.runningwith.domain.event.EnrollmentEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class EnrollmentEvent {
    protected final EnrollmentEntity enrollmentEntity;
    protected final String message;
}
