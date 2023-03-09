package com.runningwith.domain.event.event;

import com.runningwith.domain.event.EnrollmentEntity;

public class EnrollmentRejectedEvent extends EnrollmentEvent {
    public EnrollmentRejectedEvent(EnrollmentEntity enrollmentEntity) {
        super(enrollmentEntity, "모임 참가 신청이 거부되었습니다.");
    }
}
