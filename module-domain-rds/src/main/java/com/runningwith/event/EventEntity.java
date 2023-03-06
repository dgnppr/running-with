package com.runningwith.event;

import com.runningwith.event.enumeration.EventType;
import com.runningwith.study.StudyEntity;
import com.runningwith.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "event")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_study")
    private StudyEntity studyEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_users")
    private UsersEntity createdBy;

    @OneToMany(mappedBy = "eventEntity")
    @OrderBy("enrolledAt")
    private List<EnrollmentEntity> enrollments = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private String description;

    @Column(nullable = false, name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(nullable = false, name = "end_enrollment_date_time")
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false, name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(nullable = false, name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(nullable = false, name = "limit_of_enrollments")
    private Integer limitOfEnrollments;

    @Column(nullable = false, name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public EventEntity(String title, String description, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime, Integer limitOfEnrollments, EventType eventType) {
        this.title = title;
        this.description = description;
        this.endEnrollmentDateTime = endEnrollmentDateTime;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.limitOfEnrollments = limitOfEnrollments;
        this.eventType = eventType;
    }

    public boolean isEnrollableFor(UsersEntity usersEntity) {
        return isNotClosed() && !isAlreadyEnrolled(usersEntity);
    }

    public boolean isDisenrollableFor(UsersEntity usersEntity) {
        return isNotClosed() && isAlreadyEnrolled(usersEntity);
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isAttended(UsersEntity usersEntity) {
        for (EnrollmentEntity e : this.enrollments) {
            if (e.getUsersEntity().equals(usersEntity) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public int numberOfRemainSpots() {
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(EnrollmentEntity::isAccepted).count();
    }

    private boolean isAlreadyEnrolled(UsersEntity usersEntity) {
        for (EnrollmentEntity e : this.enrollments) {
            if (e.getUsersEntity().equals(usersEntity)) {
                return true;
            }
        }
        return false;
    }

    public EventEntity create(StudyEntity studyEntity, UsersEntity createdBy) {
        this.studyEntity = studyEntity;
        this.createdBy = createdBy;
        this.createdDateTime = LocalDateTime.now();
        return this;
    }

    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(EnrollmentEntity::isAccepted).count();
    }

    public void update(String title, String description, EventType eventType, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime, Integer limitOfEnrollments) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.endEnrollmentDateTime = endEnrollmentDateTime;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.limitOfEnrollments = limitOfEnrollments;
    }

    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    public void addNewEnrollment(EnrollmentEntity enrollmentEntity) {
        this.enrollments.add(enrollmentEntity);
        enrollmentEntity.setEvent(this);
    }

    public void removeEnrollment(EnrollmentEntity enrollmentEntity) {
        this.enrollments.remove(enrollmentEntity);
        enrollmentEntity.setEvent(null);
    }

    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            EnrollmentEntity enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if (enrollmentToAccept != null) {
                enrollmentToAccept.updateAccepted(true);
            }
        }
    }

    private EnrollmentEntity getTheFirstWaitingEnrollment() {
        for (EnrollmentEntity e : this.enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }

        return null;
    }
}