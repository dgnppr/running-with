package com.runningwith.event;

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
    @JoinColumn(name = "id_account")
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

}