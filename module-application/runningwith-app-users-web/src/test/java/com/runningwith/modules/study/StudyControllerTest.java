package com.runningwith.modules.study;

import com.runningwith.infra.MockMvcTest;
import com.runningwith.modules.account.AccountEntity;
import com.runningwith.modules.account.AccountRepository;
import com.runningwith.modules.account.enumeration.AccountType;
import com.runningwith.modules.study.factory.StudyEntityFactory;
import com.runningwith.modules.study.form.StudyForm;
import com.runningwith.modules.users.UsersEntity;
import com.runningwith.modules.users.UsersRepository;
import com.runningwith.modules.users.WithUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.infra.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.infra.utils.WebUtils.REDIRECT;
import static com.runningwith.modules.AppExceptionHandler.VIEW_ERROR;
import static com.runningwith.modules.study.StudyController.*;
import static com.runningwith.modules.users.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.modules.users.WithUserSecurityContextFactory.PASSWORD;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudyControllerTest {

    public static final String TESTPATH = "testpath";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyService studyService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StudyEntityFactory studyEntityFactory;

    @BeforeEach
    void setUp() {
        UsersEntity usersEntity = createNewUser();
        StudyForm studyForm = studyEntityFactory.createStudyForm(TESTPATH, "testpath", "testpath", "testpath");
        studyEntityFactory.createStudyEntity(usersEntity, studyForm.toEntity());
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @WithUser
    @DisplayName("스터디 폼 뷰")
    @Test
    void view_new_study_form() throws Exception {
        mockMvc.perform(get(URL_NEW_STUDY))
                .andExpect(model().attributeExists(FORM_STUDY))
                .andExpect(model().attributeExists("user"))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_STUDY_FORM));
    }

    @WithUser
    @DisplayName("스터디 생성 - 입력값 정상")
    @Test
    void create_new_study_with_correct_inputs() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm newStudyForm = studyEntityFactory.createStudyForm("teststudy", "testtitle", "short description", "full description");
        String encodedPath = URLEncoder.encode(newStudyForm.getPath(), StandardCharsets.UTF_8);

        mockMvc.perform(post(URL_NEW_STUDY)
                        .param("path", newStudyForm.getPath())
                        .param("title", newStudyForm.getTitle())
                        .param("shortDescription", newStudyForm.getShortDescription())
                        .param("fullDescription", newStudyForm.getFullDescription())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT + URL_STUDY_PATH + encodedPath));

        assertThatStudyCreated(usersEntity, newStudyForm);
    }

    @WithUser
    @DisplayName("스터디 생성 - 입력값 오류")
    @Test
    void create_new_study_with_wrong_inputs() throws Exception {

        StudyForm newStudyForm = studyEntityFactory.createStudyForm("1", "1", "1", "1");

        mockMvc.perform(post(URL_NEW_STUDY)
                        .param("path", newStudyForm.getPath())
                        .param("title", newStudyForm.getTitle())
                        .param("shortDescription", newStudyForm.getShortDescription())
                        .param("fullDescription", newStudyForm.getFullDescription())
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_STUDY_FORM));
    }

    @WithUser
    @DisplayName("스터디 조회 뷰 - 경로 정상")
    @Test
    void view_study_path_with_correct_path() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + getEncodedUrl(TESTPATH)))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(view().name(VIEW_STUDY));
    }

    @WithUser
    @DisplayName("스터디 조회 뷰 - 경로 오류")
    @Test
    void view_study_path_with_wrong_path() throws Exception {
        String testpath = getEncodedUrl("wrongtestpath");
        mockMvc.perform(get(URL_STUDY_PATH + getEncodedUrl(testpath)))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attributeExists("error"))
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(IllegalArgumentException.class, ex.getClass());
                })
                .andExpect(view().name(VIEW_ERROR));
    }

    @WithUser
    @DisplayName("스터디 멤버 조회")
    @Test
    void view_study_members_by_managers() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_STUDY_MEMBERS))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(view().name(VIEW_STUDY_MEMBERS));
    }

    @WithUser
    @DisplayName("스터디 참여")
    @Test
    void join_study() throws Exception {
        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_STUDY_JOIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + TESTPATH + URL_STUDY_MEMBERS));

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        assertThatIsStudyMember(usersEntity, studyEntity);
    }

    @WithUser
    @DisplayName("스터디 탈퇴")
    @Test
    void leave_study() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        studyService.addMember(TESTPATH, usersEntity);

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_STUDY_LEAVE))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + TESTPATH + URL_STUDY_MEMBERS));

        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        assertThatIsNotStudyMember(usersEntity, studyEntity);
    }


    private UsersEntity createNewUser() {
        UsersEntity newUsersEntity = UsersEntity.builder()
                .nickname("nickname")
                .email("nickname" + EMAIL)
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

    private void assertThatStudyCreated(UsersEntity usersEntity, StudyForm newStudyForm) {
        Optional<StudyEntity> studyEntityOptional = studyRepository.findByPath(newStudyForm.getPath());
        assertThat(studyEntityOptional.isPresent()).isTrue();
        StudyEntity studyEntity = studyEntityOptional.get();
        assertThat(studyEntity.getPath()).isEqualTo(newStudyForm.getPath());
        assertThat(studyEntity.getTitle()).isEqualTo(newStudyForm.getTitle());
        assertThat(studyEntity.getShortDescription()).isEqualTo(newStudyForm.getShortDescription());
        assertThat(studyEntity.getFullDescription()).isEqualTo(newStudyForm.getFullDescription());
        assertThat(studyEntity.getManagers().contains(usersEntity)).isTrue();
    }

    private void assertThatIsStudyMember(UsersEntity usersEntity, StudyEntity studyEntity) {
        Assertions.assertThat(studyEntity.getMembers()).contains(usersEntity);
    }

    private void assertThatIsNotStudyMember(UsersEntity usersEntity, StudyEntity studyEntity) {
        Assertions.assertThat(studyEntity.getMembers()).doesNotContain(usersEntity);
    }
}