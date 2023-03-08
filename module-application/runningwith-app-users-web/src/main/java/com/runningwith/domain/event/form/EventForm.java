package com.runningwith.domain.event.form;

import com.runningwith.domain.event.EventEntity;
import com.runningwith.domain.event.enumeration.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    private String description;

    private EventType eventType = EventType.FCFS;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @Min(2)
    private Integer limitOfEnrollments = 2;

    public static EventForm toForm(EventEntity eventEntity) {
        return EventForm.builder()
                .title(eventEntity.getTitle())
                .description(eventEntity.getDescription())
                .eventType(eventEntity.getEventType())
                .endEnrollmentDateTime(eventEntity.getEndEnrollmentDateTime())
                .startDateTime(eventEntity.getStartDateTime())
                .endDateTime(eventEntity.getEndDateTime())
                .limitOfEnrollments(eventEntity.getLimitOfEnrollments())
                .build();
    }

    public EventEntity toEntity() {
        return new EventEntity(this.title, this.description, this.endEnrollmentDateTime, this.startDateTime, this.endDateTime, this.limitOfEnrollments, this.eventType);
    }
}
