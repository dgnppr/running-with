package com.runningwith.study;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
import com.runningwith.study.form.StudyDescriptionForm;
import com.runningwith.study.form.StudyForm;
import com.runningwith.users.UsersEntity;
import com.runningwith.users.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.runningwith.AppExceptionHandler.VIEW_ERROR;
import static com.runningwith.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.WithUserSecurityContextFactory.PASSWORD;
import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.study.StudySettingsController.*;
import static com.runningwith.utils.CustomStringUtils.*;
import static com.runningwith.utils.WebUtils.REDIRECT;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest {

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
        usersRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @WithUser
    @DisplayName("스터디 소개글 수정 뷰 - by manager")
    @Test
    void view_study_setting_description_by_managers() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TESTPATH + URL_STUDY_SETTINGS_DESCRIPTION))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attributeExists(STUDY_DESCRIPTION_FORM))
                .andExpect(view().name(VIEW_STUDY_SETTINGS_DESCRIPTION));
    }

    @WithUser
    @DisplayName("스터디 소개글 수정 뷰 - by other")
    @Test
    void view_study_setting_description_by_other() throws Exception {
        UsersEntity otherUser = saveOtherUser();
        StudyForm studyForm = new StudyForm("ttestpath", "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyService.createNewStudy(otherUser, studyEntity);

        mockMvc.perform(get(URL_STUDY_PATH + "ttestpath" + URL_STUDY_SETTINGS_DESCRIPTION))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(AccessDeniedException.class, ex.getClass());
                })
                .andExpect(view().name(VIEW_ERROR));
    }

    @WithUser
    @DisplayName("스터디 소개글 업데이트 - 입력값 정상")
    @Test
    void update_study_setting_description_with_correct_inputs() throws Exception {
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        StudyDescriptionForm form = new StudyDescriptionForm("test", "test");

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_STUDY_SETTINGS_DESCRIPTION)
                        .param("shortDescription", form.getShortDescription())
                        .param("fullDescription", form.getFullDescription())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_STUDY_PATH + getEncodedUrl(studyEntity.getPath()) + URL_STUDY_SETTINGS_DESCRIPTION));

        StudyEntity study = studyRepository.findByPath(TESTPATH).get();
        assertThat(study.getShortDescription()).isEqualTo(form.getShortDescription());
        assertThat(study.getFullDescription()).isEqualTo(form.getFullDescription());
    }

    @WithUser
    @DisplayName("스터디 소개글 업데이트 - 입력값 오류")
    @Test
    void update_study_setting_description_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();
        StudyDescriptionForm form = new StudyDescriptionForm("", "");

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_STUDY_SETTINGS_DESCRIPTION)
                        .param("shortDescription", form.getShortDescription())
                        .param("fullDescription", form.getFullDescription())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_STUDY_SETTINGS_DESCRIPTION));
    }

    @WithUser
    @DisplayName("스터디 배너 이미지 뷰")
    @Test
    void view_study_banner_image() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TESTPATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + "TESTPATH" + URL_STUDY_SETTINGS_BANNER))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(view().name(VIEW_STUDY_SETTINGS_BANNER));
    }

    @WithUser
    @DisplayName("스터디 배너 이미지 업데이트")
    @Test
    void update_study_banner_image() throws Exception {

        String origin = studyRepository.findByPath(TESTPATH).get().getBannerImage();

        mockMvc.perform(post(URL_STUDY_PATH + TESTPATH + URL_STUDY_SETTINGS_BANNER)
                        .param("image", RANDOM_STRING)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(flash().attribute("message", "배너 이미지 수정 완료"))
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TESTPATH) + URL_STUDY_SETTINGS_BANNER));

        String changed = studyRepository.findByPath(TESTPATH).get().getBannerImage();

        assertThat(changed).isNotEqualTo(origin);
    }


    private UsersEntity saveOtherUser() {
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
}