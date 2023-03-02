package com.runningwith.event.form;

import com.runningwith.event.EventEntity;
import com.runningwith.event.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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

    public EventEntity toEntity() {
        return new EventEntity(this.title, this.description, this.endEnrollmentDateTime, this.startDateTime, this.endDateTime, this.limitOfEnrollments, this.eventType);
    }
}
