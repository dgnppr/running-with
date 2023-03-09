package com.runningwith.domain.event.event;

import com.runningwith.domain.event.EnrollmentEntity;

public class EnrollmentAcceptedEvent extends EnrollmentEvent {
    public EnrollmentAcceptedEvent(EnrollmentEntity enrollmentEntity) {
        super(enrollmentEntity, "모임 참가 신청이 승인되었습니다.");
    }
}
