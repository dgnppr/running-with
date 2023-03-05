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
    public static final String VIEW_EVENT_VIEW = "event/view";
    public static final String VIEW_STUDY_EVENTS = "study/events";
    public static final String URL_EVENT_EDIT = "/edit";
    public static final String VIEW_EVENT_EDIT = "event/edit";
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
    @GetMapping(URL_EVENTS + URL_SLASH + "{id}")
    public String viewEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id, Model model) {

        StudyEntity studyEntity = studyService.getStudy(path);
        EventEntity eventEntity = getOrElseThrow(id);

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

    @GetMapping(URL_EVENTS + URL_SLASH + "{id}" + URL_EVENT_EDIT)
    public String viewEventUpdate(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                  @PathVariable Long id, Model model) {

        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        EventEntity eventEntity = getOrElseThrow(id);

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        model.addAttribute("event", eventEntity);
        model.addAttribute(EVENT_FORM, EventForm.toForm(eventEntity));

        return VIEW_EVENT_EDIT;
    }

    @PostMapping(URL_EVENTS + URL_SLASH + "{id}" + URL_EVENT_EDIT)
    public String updateEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {

        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);

        return VIEW_EVENT_EDIT;
    }

    private EventEntity getOrElseThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
    }

}
