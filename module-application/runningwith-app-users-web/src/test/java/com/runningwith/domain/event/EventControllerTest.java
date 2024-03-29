package com.runningwith.domain.event;

import com.runningwith.domain.event.event.EnrollmentAcceptedEvent;
import com.runningwith.domain.event.event.EnrollmentEventListener;
import com.runningwith.domain.event.event.EnrollmentRejectedEvent;
import com.runningwith.domain.event.factory.EventEntityFactory;
import com.runningwith.domain.event.form.EventForm;
import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.study.StudyRepository;
import com.runningwith.domain.study.StudyService;
import com.runningwith.domain.study.factory.StudyEntityFactory;
import com.runningwith.domain.study.form.StudyForm;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.WithUser;
import com.runningwith.domain.users.factory.UsersEntityFactory;
import com.runningwith.infra.MockMvcTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.runningwith.domain.event.EventController.*;
import static com.runningwith.domain.event.enumeration.EventType.CONFIRMATIVE;
import static com.runningwith.domain.event.enumeration.EventType.FCFS;
import static com.runningwith.domain.main.ExceptionAdvice.VIEW_ERROR;
import static com.runningwith.domain.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.infra.utils.CustomStringUtils.getEncodedUrl;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
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

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventService eventService;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    EventEntityFactory eventEntityFactory;

    @Autowired
    UsersEntityFactory usersEntityFactory;

    @Autowired
    StudyEntityFactory studyEntityFactory;

    @MockBean
    EnrollmentEventListener enrollmentEventListener;

    @BeforeEach
    void setUp() {
        UsersEntity studyCreator = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm studyForm = studyEntityFactory.createStudyForm(TESTPATH, "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyService.createNewStudy(studyCreator, studyEntity);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
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
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title", 2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);

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
                .andExpect(redirectedUrlPattern(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + "{id}"));
    }

    @WithUser
    @DisplayName("스터디 모임 생성 - 입력값 오류")
    @Test
    void submit_study_event_form_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        EventForm eventForm = eventEntityFactory.createEventForm("", "", 0, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);

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

    @WithUser
    @DisplayName("스터디 모임 상세 뷰 - 경로 정상")
    @Test
    void view_study_event_with_correct_path() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title", 2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("event", eventEntity))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EVENT_VIEW));
    }

    @WithUser
    @DisplayName("스터디 모임 상세 뷰 - 경로 오류")
    @Test
    void view_study_event_with_wrong_path() throws Exception {
        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + "-1"))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ERROR));
    }

    @WithUser
    @DisplayName("스터디 모임 리스트 뷰")
    @Test
    void view_study_event_list() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_STUDY_EVENTS));
    }

    @WithUser
    @DisplayName("스터디 모임 수정 뷰")
    @Test
    void view_study_event_edit() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title", 2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_EDIT))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("event", eventEntity))
                .andExpect(model().attributeExists(EVENT_FORM))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EVENT_EDIT));
    }

    @WithUser
    @DisplayName("스터디 모임 수정 - 입력값 정상")
    @Test
    void post_study_event_with_correct_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm beforeForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity beforeEvent = eventEntityFactory.createEvent(beforeForm.toEntity(), studyEntity, usersEntity);
        EventForm afterForm = eventEntityFactory.createEventForm("event From description", "event From title",
                3, now().plusDays(2), now().plusDays(3), now().plusDays(4), FCFS);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + beforeEvent.getId() + URL_EVENT_EDIT)
                        .param("eventType", afterForm.getEventType().toString())
                        .param("description", afterForm.getDescription())
                        .param("title", afterForm.getTitle())
                        .param("endEnrollmentDateTime", afterForm.getEndEnrollmentDateTime().toString())
                        .param("startDateTime", afterForm.getStartDateTime().toString())
                        .param("endDateTime", afterForm.getEndDateTime().toString())
                        .param("limitOfEnrollments", afterForm.getLimitOfEnrollments().toString())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + beforeEvent.getId()));

        EventEntity afterEvent = eventRepository.findById(beforeEvent.getId()).get();
        assertThatEventChanged(afterForm, afterEvent);
    }
    // TODO check removed with enrollments

    @WithUser
    @DisplayName("스터디 모임 수정 - 입력값 오류")
    @Test
    void post_study_event_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm beforeForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity beforeEvent = eventEntityFactory.createEvent(beforeForm.toEntity(), studyEntity, usersEntity);
        EventForm afterForm = eventEntityFactory.createEventForm("event From description", "event From title",
                3, now().minusDays(2), now().plusDays(3), now().plusDays(4), CONFIRMATIVE);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + beforeEvent.getId() + URL_EVENT_EDIT)
                        .param("eventType", afterForm.getEventType().toString())
                        .param("description", afterForm.getDescription())
                        .param("title", afterForm.getTitle())
                        .param("endEnrollmentDateTime", afterForm.getEndEnrollmentDateTime().toString())
                        .param("startDateTime", afterForm.getStartDateTime().toString())
                        .param("endDateTime", afterForm.getEndDateTime().toString())
                        .param("limitOfEnrollments", afterForm.getLimitOfEnrollments().toString())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors(EVENT_FORM))
                .andExpect(model().attributeHasFieldErrors("eventForm", "endEnrollmentDateTime"))
                .andExpect(view().name(VIEW_EVENT_EDIT));
    }

    @WithUser
    @DisplayName("스터디 모임 삭제")
    @Test
    void delete_study_event() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_DELETE)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS));

        assertThatEventDeleted(eventEntity);
    }

    @WithUser
    @DisplayName("선착순 스터디 모임 참가 신청 - 자동 수락")
    @Test
    void submit_new_enrollment_to_FCFS_accepted() throws Exception {
        UsersEntity usersEntity = usersEntityFactory.createUsersEntity("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_ENROLL)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        UsersEntity applicant = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        assertThatAccepted(eventEntity, applicant);
    }

    @WithUser
    @DisplayName("선착순 스터디 모임 참가 신청 - 대기(인원 차 있는 상태)")
    @Test
    void submit_new_enrollment_to_FCFS_waiting() throws Exception {
        UsersEntity eventCreator = usersEntityFactory.createUsersEntity("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);

        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, eventCreator);

        UsersEntity test1 = usersEntityFactory.createUsersEntity("test1");
        UsersEntity test2 = usersEntityFactory.createUsersEntity("test2");
        eventService.newEnrollment(eventEntity, test1);
        eventService.newEnrollment(eventEntity, test2);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_ENROLL)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        UsersEntity applicant = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        assertThatNotAccepted(eventEntity, applicant);
    }

    @WithUser
    @DisplayName("선착순 모임 참가 신청 취소 by 참가 신청 확정자 when 대기자가 있는 경우")
    @Test
    void accept_next_applicant_to_FCFS_event_when_accepted_applicant_cancel() throws Exception {
        UsersEntity eventCreator = usersEntityFactory.createUsersEntity("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);

        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, eventCreator);

        UsersEntity canceler = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        UsersEntity applicant1 = usersEntityFactory.createUsersEntity("applicant1");
        UsersEntity applicant2 = usersEntityFactory.createUsersEntity("applicant2");
        eventService.newEnrollment(eventEntity, canceler);
        eventService.newEnrollment(eventEntity, applicant1);
        eventService.newEnrollment(eventEntity, applicant2);

        assertThatAccepted(eventEntity, canceler);
        assertThatAccepted(eventEntity, applicant1);
        assertThatNotAccepted(eventEntity, applicant2);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_DISENROLL)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatAccepted(eventEntity, applicant1);
        assertThatAccepted(eventEntity, applicant2);
        assertThatEnrollmentCanceled(eventEntity, canceler);
    }

    @WithUser
    @DisplayName("선착순 모임 참가 신청 취소 by 참기 신청 대기자")
    @Test
    void not_accepted_user_cancel_enrollment_to_FCFS() throws Exception {
        UsersEntity eventCreator = usersEntityFactory.createUsersEntity("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, eventCreator);

        UsersEntity applicant1 = usersEntityFactory.createUsersEntity("applicant1");
        UsersEntity applicant2 = usersEntityFactory.createUsersEntity("applicant2");
        UsersEntity canceler = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        eventService.newEnrollment(eventEntity, applicant1);
        eventService.newEnrollment(eventEntity, applicant2);
        eventService.newEnrollment(eventEntity, canceler);

        assertThatAccepted(eventEntity, applicant1);
        assertThatAccepted(eventEntity, applicant2);
        assertThatNotAccepted(eventEntity, canceler);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_DISENROLL)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatAccepted(eventEntity, applicant1);
        assertThatAccepted(eventEntity, applicant2);
        assertThatEnrollmentCanceled(eventEntity, canceler);
    }

    @WithUser
    @DisplayName("관리자 확인 모임 참가 신청 - 대기 상태")
    @Test
    void submit_new_enrollment_to_confirmative_waiting() throws Exception {
        UsersEntity eventCreator = usersEntityFactory.createUsersEntity("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), CONFIRMATIVE);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, eventCreator);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_ENROLL)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        UsersEntity applicant = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        assertThatNotAccepted(eventEntity, applicant);
    }

    @WithUser
    @DisplayName("관리자 확인 모임 참가 신청 수락 by manager")
    @Test
    void accept_new_enrollment_to_confirmative_event() throws Exception {
        UsersEntity manager = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), CONFIRMATIVE);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, manager);

        UsersEntity applicant = usersEntityFactory.createUsersEntity("applicant");
        eventService.newEnrollment(eventEntity, applicant);
        assertThatNotAccepted(eventEntity, applicant);

        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_ENROLLMENTS_PATH + enrollmentEntity.getId() + URL_ENROLLMENT_ACCEPT))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatAccepted(eventEntity, applicant);
        then(enrollmentEventListener).should().handleEnrollmentEvent(any(EnrollmentAcceptedEvent.class));
    }

    @WithUser
    @DisplayName("관리자 확인 모임 참가 신청 거절 by manager")
    @Test
    void reject_new_enrollment_to_confirmative_event() throws Exception {
        UsersEntity manager = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), CONFIRMATIVE);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, manager);

        UsersEntity applicant = usersEntityFactory.createUsersEntity("applicant");
        eventService.newEnrollment(eventEntity, applicant);
        assertThatNotAccepted(eventEntity, applicant);

        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_ENROLLMENTS_PATH + enrollmentEntity.getId() + URL_ENROLLMENT_REJECT))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatNotAccepted(eventEntity, applicant);
        then(enrollmentEventListener).should().handleEnrollmentEvent(any(EnrollmentRejectedEvent.class));
    }

    @WithUser
    @DisplayName("참가 체크인 by manager")
    @Test
    void check_in_enrollment() throws Exception {
        UsersEntity manager = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, manager);

        UsersEntity applicant = usersEntityFactory.createUsersEntity("applicant");
        eventService.newEnrollment(eventEntity, applicant);
        assertThatAccepted(eventEntity, applicant);

        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_ENROLLMENTS_PATH + enrollmentEntity.getId() + URL_ENROLLMENT_CHECK_IN))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatEnrollmentCheckedIn(eventEntity, applicant);
    }

    @WithUser
    @DisplayName("참가 체크인 취소 by manager")
    @Test
    void cancel_check_in_enrollment() throws Exception {
        UsersEntity manager = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = eventEntityFactory.createEventForm("eventFrom description", "eventFrom title",
                2, now().plusDays(1), now().plusDays(2), now().plusDays(3), FCFS);
        EventEntity eventEntity = eventEntityFactory.createEvent(eventForm.toEntity(), studyEntity, manager);

        UsersEntity applicant = usersEntityFactory.createUsersEntity("applicant");
        eventService.newEnrollment(eventEntity, applicant);
        assertThatAccepted(eventEntity, applicant);
        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant).get();
        eventService.checkInEnrollment(enrollmentEntity);

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_ENROLLMENTS_PATH + enrollmentEntity.getId() + URL_ENROLLMENT_CANCEL_CHECK_IN))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS_PATH + eventEntity.getId()));

        assertThatEnrollmentNotCheckedIn(eventEntity, applicant);
    }

    private void assertThatEnrollmentNotCheckedIn(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollment).isPresent();
        EnrollmentEntity enrollment = optionalEnrollment.get();
        assertThat(enrollment.isAttended()).isFalse();
    }

    private void assertThatEnrollmentCheckedIn(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollment).isPresent();
        EnrollmentEntity enrollment = optionalEnrollment.get();
        assertThat(enrollment.isAttended()).isTrue();
    }


    private void assertThatEnrollmentCanceled(EventEntity eventEntity, UsersEntity canceler) {
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, canceler);
        assertThat(optionalEnrollment).isEmpty();
    }

    private void assertThatAccepted(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollmentEntity).isPresent();
        EnrollmentEntity enrollmentEntity = optionalEnrollmentEntity.get();
        assertThat(enrollmentEntity.isAccepted()).isTrue();
    }

    private void assertThatEventDeleted(EventEntity eventEntity) {
        assertThat(eventRepository.findById(eventEntity.getId())).isEmpty();
    }

    private void assertThatNotAccepted(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollmentEntity).isPresent();
        EnrollmentEntity enrollmentEntity = optionalEnrollmentEntity.get();
        assertThat(enrollmentEntity.isAccepted()).isFalse();
    }

    private void assertThatEventChanged(EventForm afterForm, EventEntity afterEvent) {
        assertThat(afterEvent.getTitle()).isEqualTo(afterForm.getTitle());
        assertThat(afterEvent.getEventType()).isEqualTo(afterForm.getEventType());
        assertThat(afterEvent.getEndEnrollmentDateTime()).isEqualTo(afterForm.getEndEnrollmentDateTime());
        assertThat(afterEvent.getStartDateTime()).isEqualTo(afterForm.getStartDateTime());
        assertThat(afterEvent.getEndDateTime()).isEqualTo(afterForm.getEndDateTime());
        assertThat(afterEvent.getLimitOfEnrollments()).isEqualTo(afterForm.getLimitOfEnrollments());
    }

}