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

import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.utils.WebUtils.REDIRECT;

@Slf4j
@Controller
@RequestMapping(URL_STUDY_PATH + "{path}")
@RequiredArgsConstructor
public class EventController {

    public static final String EVENT_FORM = "eventForm";
    public static final String VIEW_EVENT_FORM = "event/form";
    public static final String URL_NEW_EVENT = "/new-event";
    public static final String URL_EVENTS = "/events/";
    public static final String VIEW_EVENT_VIEW = "event/view";
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
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_EVENTS + eventEntity.getId();
    }

    // TODO event view design
    @GetMapping(URL_EVENTS + "{id}")
    public String getEvent(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @PathVariable Long id, Model model) {

        StudyEntity studyEntity = studyService.getStudy(path);
        EventEntity eventEntity = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

        model.addAttribute("user", usersEntity);
        model.addAttribute("event", eventEntity);
        model.addAttribute("study", studyEntity);

        return VIEW_EVENT_VIEW;
    }

}
