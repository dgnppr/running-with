package com.runningwith.event;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.event.enumeration.EventType;
import com.runningwith.event.form.EventForm;
import com.runningwith.study.StudyEntity;
import com.runningwith.study.StudyRepository;
import com.runningwith.study.StudyService;
import com.runningwith.study.form.StudyForm;
import com.runningwith.users.UsersEntity;
import com.runningwith.users.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.runningwith.event.EventController.*;
import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class EventControllerTest {

    public static final String TESTPATH = "testpath";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    StudyService studyService;

    @Autowired
    StudyRepository studyRepository;

    private EventForm getEventForm(String eventFrom_description, String eventFrom_title, int limitOfEnrollments) {
        EventForm eventForm = new EventForm();
        eventForm.setEventType(EventType.FCFS);
        eventForm.setDescription(eventFrom_description);
        eventForm.setTitle(eventFrom_title);
        eventForm.setLimitOfEnrollments(limitOfEnrollments);
        eventForm.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        eventForm.setStartDateTime(LocalDateTime.now().plusDays(2));
        eventForm.setEndDateTime(LocalDateTime.now().plusDays(3));
        return eventForm;
    }

    @BeforeEach
    void setUp() {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm studyForm = new StudyForm(TESTPATH, "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyService.createNewStudy(usersEntity, studyEntity);
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
    }

    @WithUser
    @DisplayName("스터디 모임 생성 뷰")
    @Test
    void view_study_event_create() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_NEW_EVENT))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attributeExists(EVENT_FORM))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EVENT_FORM));
    }

    @WithUser
    @DisplayName("스터디 모임 생성 - 입력값 정상")
    @Test
    void submit_study_event_form_with_correct_inputs() throws Exception {
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title", 2);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_NEW_EVENT)
                        .param("eventType", eventForm.getEventType().toString())
                        .param("description", eventForm.getDescription())
                        .param("title", eventForm.getTitle())
                        .param("endEnrollmentDateTime", eventForm.getEndEnrollmentDateTime().toString())
                        .param("startDateTime", eventForm.getStartDateTime().toString())
                        .param("endDateTime", eventForm.getEndDateTime().toString())
                        .param("limitOfEnrollments", eventForm.getLimitOfEnrollments().toString())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS + "{id}"));
    }

    @WithUser
    @DisplayName("스터디 모임 생성 - 입력값 오류")
    @Test
    void submit_study_event_form_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        EventForm eventForm = getEventForm("", "", 0);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_NEW_EVENT)
                        .param("eventType", eventForm.getEventType().toString())
                        .param("description", eventForm.getDescription())
                        .param("title", eventForm.getTitle())
                        .param("endEnrollmentDateTime", eventForm.getEndEnrollmentDateTime().toString())
                        .param("startDateTime", eventForm.getStartDateTime().toString())
                        .param("endDateTime", eventForm.getEndDateTime().toString())
                        .param("limitOfEnrollments", eventForm.getLimitOfEnrollments().toString())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EVENT_FORM));

    }


}