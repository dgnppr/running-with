package com.runningwith.event;

import com.runningwith.event.form.EventForm;
import com.runningwith.event.validator.EventValidator;
import com.runningwith.study.StudyEntity;
import com.runningwith.study.StudyService;
import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.utils.WebUtils.REDIRECT;
import static com.runningwith.utils.WebUtils.URL_SLASH;

@Slf4j
@Controller
@RequestMapping(URL_STUDY_PATH + "{path}")
@RequiredArgsConstructor
public class EventController {

    public static final String EVENT_FORM = "eventForm";
    public static final String VIEW_EVENT_FORM = "event/form";
    public static final String URL_NEW_EVENT = "/new-event";
    public static final String URL_EVENTS = "/events";
    public static final String URL_EVENTS_PATH = "/events/";
    public static final String VIEW_EVENT_VIEW = "event/view";
    public static final String VIEW_STUDY_EVENTS = "study/events";
    public static final String URL_EVENT_EDIT = "/edit";
    public static final String VIEW_EVENT_EDIT = "event/edit";
    public static final String URL_EVENT_DELETE = "/delete";
    public static final String URL_EVENT_ENROLL = "/enroll";
    public static final String URL_EVENT_DISENROLL = "/disenroll";
    public static final String URL_ENROLLMENTS_PATH = "/enrollments/";
    public static final String URL_ENROLLMENT_ACCEPT = "/accept";
    public static final String URL_ENROLLMENT_REJECT = "/reject";
    private final StudyService studyService;
    private final EventService eventService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder(EVENT_FORM)
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping(URL_NEW_EVENT)
    public String viewNewEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);
        model.addAttribute("study", studyEntity);
        model.addAttribute("user", usersEntity);
        model.addAttribute(new EventForm());
        return VIEW_EVENT_FORM;
    }

    @PostMapping(URL_NEW_EVENT)
    public String submitNewEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                 @Validated EventForm eventForm, BindingResult bindingResult, Model model) {

        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);

        if (bindingResult.hasErrors()) {
            model.addAttribute("study", studyEntity);
            model.addAttribute("user", usersEntity);
            return VIEW_EVENT_FORM;
        }

        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS + URL_SLASH + eventEntity.getId();
    }

    // TODO event view redesign
    @GetMapping(URL_EVENTS_PATH + "{id}")
    public String viewEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id, Model model) {

        StudyEntity studyEntity = studyService.getStudy(path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);

        model.addAttribute("user", usersEntity);
        model.addAttribute("event", eventEntity);
        model.addAttribute("study", studyEntity);

        return VIEW_EVENT_VIEW;
    }

    @GetMapping(URL_EVENTS)
    public String viewEvents(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudy(path);

        List<EventEntity> newEvents = new ArrayList<>();
        List<EventEntity> oldEvents = new ArrayList<>();

        eventRepository.findByStudyEntityOrderByStartDateTime(studyEntity).forEach(eventEntity -> {
            if (eventEntity.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(eventEntity);
            } else {
                newEvents.add(eventEntity);
            }
        });

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return VIEW_STUDY_EVENTS;
    }

    @GetMapping(URL_EVENTS_PATH + "{id}" + URL_EVENT_EDIT)
    public String viewEventUpdate(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                  @PathVariable Long id, Model model) {

        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        model.addAttribute("event", eventEntity);
        model.addAttribute(EVENT_FORM, EventForm.toForm(eventEntity));

        return VIEW_EVENT_EDIT;
    }

    @PostMapping(URL_EVENTS_PATH + "{id}" + URL_EVENT_EDIT)
    public String updateEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id,
                              @Validated EventForm eventForm, BindingResult bindingResult, Model model) {

        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);
        eventForm.setEventType(eventEntity.getEventType());
        eventValidator.validateUpdateForm(eventForm, eventEntity, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            model.addAttribute("study", studyEntity);
            model.addAttribute("event", eventEntity);
            return VIEW_EVENT_EDIT;
        }

        eventService.updateEvent(eventEntity, eventForm);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS_PATH + eventEntity.getId();
    }

    @PostMapping(URL_EVENTS_PATH + "{id}" + URL_EVENT_DELETE)
    public String cancelEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);
        eventService.deleteEvent(eventEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS;
    }

    @PostMapping(URL_EVENTS_PATH + "{id}" + URL_EVENT_ENROLL)
    public String newEnrollment(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id) {
        StudyEntity studyEntity = studyService.getStudyToEnroll(path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);

        eventService.newEnrollment(eventEntity, usersEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS_PATH + id;
    }

    @PostMapping(URL_EVENTS_PATH + "{id}" + URL_EVENT_DISENROLL)
    public String cancelEnrollment(@CurrentUser UsersEntity usersEntity,
                                   @PathVariable String path, @PathVariable Long id) {
        StudyEntity studyEntity = studyService.getStudyToEnroll(path);
        EventEntity eventEntity = getEventEntityOrElseThrow(id);

        eventService.cancelEnrollment(eventEntity, usersEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS_PATH + id;
    }

    @GetMapping(URL_EVENTS_PATH + "{id}" + URL_ENROLLMENTS_PATH + "{enrollmentId}" + URL_ENROLLMENT_ACCEPT)
    public String acceptEnrollment(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                   @PathVariable("id") EventEntity eventEntity, @PathVariable("enrollmentId") EnrollmentEntity enrollmentEntity) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        eventService.acceptEnrollment(eventEntity, enrollmentEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS_PATH + eventEntity.getId();
    }

    @GetMapping(URL_EVENTS_PATH + "{id}" + URL_ENROLLMENTS_PATH + "{enrollmentId}" + URL_ENROLLMENT_REJECT)
    public String rejectEnrollment(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                   @PathVariable("id") EventEntity eventEntity, @PathVariable("enrollmentId") EnrollmentEntity enrollmentEntity) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        eventService.rejectEnrollment(eventEntity, enrollmentEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS_PATH + eventEntity.getId();
    }


    private EventEntity getEventEntityOrElseThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
    }


}
