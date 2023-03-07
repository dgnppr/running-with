package com.runningwith.event;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.enumeration.AccountType;
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
import java.util.Optional;
import java.util.UUID;

import static com.runningwith.AppExceptionHandler.VIEW_ERROR;
import static com.runningwith.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.WithUserSecurityContextFactory.PASSWORD;
import static com.runningwith.event.EventController.*;
import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static org.assertj.core.api.Assertions.assertThat;
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
    AccountRepository accountRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    // TODO 스터디 모임 신청 취소
    private static void assertThatEventChanged(EventForm afterForm, EventEntity afterEvent) {
        assertThat(afterEvent.getTitle()).isEqualTo(afterForm.getTitle());
        assertThat(afterEvent.getEventType()).isEqualTo(afterForm.getEventType());
        assertThat(afterEvent.getEndEnrollmentDateTime()).isEqualTo(afterForm.getEndEnrollmentDateTime());
        assertThat(afterEvent.getStartDateTime()).isEqualTo(afterForm.getStartDateTime());
        assertThat(afterEvent.getEndDateTime()).isEqualTo(afterForm.getEndDateTime());
        assertThat(afterEvent.getLimitOfEnrollments()).isEqualTo(afterForm.getLimitOfEnrollments());
    }

    private EventForm getEventForm(String eventFrom_description, String eventFrom_title, int limitOfEnrollments, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime, EventType eventType) {
        EventForm eventForm = new EventForm();
        eventForm.setEventType(eventType);
        eventForm.setDescription(eventFrom_description);
        eventForm.setTitle(eventFrom_title);
        eventForm.setLimitOfEnrollments(limitOfEnrollments);
        eventForm.setEndEnrollmentDateTime(endEnrollmentDateTime);
        eventForm.setStartDateTime(startDateTime);
        eventForm.setEndDateTime(endDateTime);
        return eventForm;
    }

    @BeforeEach
    void setUp() {
        UsersEntity studyCreator = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm studyForm = new StudyForm(TESTPATH, "testpath", "testpath", "testpath");
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
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title", 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);

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

        EventForm eventForm = getEventForm("", "", 0, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);

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
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title", 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

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
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title", 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_EDIT))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("event", eventEntity))
                .andExpect(model().attributeExists(EVENT_FORM))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EVENT_EDIT));
    }

    // TODO 필드 검증 로직 추가

    @WithUser
    @DisplayName("스터디 모임 수정 - 입력값 정상")
    @Test
    void post_study_event_with_correct_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm beforeForm = getEventForm("eventFrom description", "eventFrom title",
                2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity beforeEvent = eventService.createEvent(beforeForm.toEntity(), studyEntity, usersEntity);
        EventForm afterForm = getEventForm("event From description", "event From title",
                3, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), EventType.FCFS);

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
    // TODO 등록된 enrollment도 같이 삭제 확인

    @WithUser
    @DisplayName("스터디 모임 수정 - 입력값 오류")
    @Test
    void post_study_event_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm beforeForm = getEventForm("eventFrom description", "eventFrom title",
                2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity beforeEvent = eventService.createEvent(beforeForm.toEntity(), studyEntity, usersEntity);
        EventForm afterForm = getEventForm("event From description", "event From title",
                3, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), EventType.CONFIRMATIVE);

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
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title",
                2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_EVENTS_PATH + eventEntity.getId() + URL_EVENT_DELETE)
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_EVENTS));

        assertThat(eventRepository.findById(eventEntity.getId())).isEmpty();
    }

    @WithUser
    @DisplayName("선착순 스터디 모임 참가 신청 - 자동 수락")
    @Test
    void submit_new_enrollment_to_FCFS_accepted() throws Exception {
        UsersEntity usersEntity = createNewUser("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title",
                2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);
        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

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
        UsersEntity usersEntity = createNewUser("nickname");
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        EventForm eventForm = getEventForm("eventFrom description", "eventFrom title",
                2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), EventType.FCFS);

        EventEntity eventEntity = eventService.createEvent(eventForm.toEntity(), studyEntity, usersEntity);

        UsersEntity test1 = createNewUser("test1");
        UsersEntity test2 = createNewUser("test2");
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


    // TODO 스터디 모임 신청 - 관리자 확인

    private void assertThatNotAccepted(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollmentEntity).isPresent();
        EnrollmentEntity enrollmentEntity = optionalEnrollmentEntity.get();
        assertThat(enrollmentEntity.isAccepted()).isFalse();
    }

    private void assertThatAccepted(EventEntity eventEntity, UsersEntity applicant) {
        Optional<EnrollmentEntity> optionalEnrollmentEntity = enrollmentRepository.findByEventEntityAndUsersEntity(eventEntity, applicant);
        assertThat(optionalEnrollmentEntity).isPresent();
        EnrollmentEntity enrollmentEntity = optionalEnrollmentEntity.get();
        assertThat(enrollmentEntity.isAccepted()).isTrue();
    }

    private UsersEntity createNewUser(String nickname) {
        UsersEntity newUsersEntity = UsersEntity.builder()
                .nickname(nickname)
                .email(nickname + EMAIL)
                .password(PASSWORD)
                .emailCheckToken(UUID.randomUUID().toString())
                .emailCheckTokenGeneratedAt(LocalDateTime.now())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .studyCreatedByEmail(false)
                .studyEnrollmentResultByEmail(false)
                .studyUpdatedByEmail(false)
                .emailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(2))
                .accountEntity(new AccountEntity(AccountType.USERS))
                .build();
        accountRepository.save(newUsersEntity.getAccountEntity());
        usersRepository.save(newUsersEntity);

        return newUsersEntity;
    }
}