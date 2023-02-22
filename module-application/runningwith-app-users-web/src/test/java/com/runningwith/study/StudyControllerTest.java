package com.runningwith.study;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.study.form.StudyForm;
import com.runningwith.users.UsersEntity;
import com.runningwith.users.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.runningwith.study.StudyController.*;
import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.utils.WebUtils.REDIRECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyService studyService;

    @BeforeEach
    void setUp() {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm studyForm = new StudyForm("testpath", "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyService.createNewStudy(usersEntity, studyEntity);
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
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
        StudyForm newStudyForm = new StudyForm("teststudy", "testtitle", "short description", "full description");
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

        Optional<StudyEntity> studyEntityOptional = studyRepository.findByPath(newStudyForm.getPath());

        assertThat(studyEntityOptional.isPresent()).isTrue();

        StudyEntity studyEntity = studyEntityOptional.get();

        assertThat(studyEntity.getPath()).isEqualTo(newStudyForm.getPath());
        assertThat(studyEntity.getTitle()).isEqualTo(newStudyForm.getTitle());
        assertThat(studyEntity.getShortDescription()).isEqualTo(newStudyForm.getShortDescription());
        assertThat(studyEntity.getFullDescription()).isEqualTo(newStudyForm.getFullDescription());
        assertThat(studyEntity.getManagers().contains(usersEntity)).isTrue();
    }

    @WithUser
    @DisplayName("스터디 생성 - 입력값 오류")
    @Test
    void create_new_study_with_wrong_inputs() throws Exception {

        StudyForm newStudyForm = new StudyForm("1", "1", "1", "1");

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
        String testpath = getEncodedUrl("testpath");
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(testpath).get();

        mockMvc.perform(get(URL_STUDY_PATH + getEncodedUrl(testpath)))
                .andExpect(status().isOk())
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
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("error"));
    }

    @WithUser
    @DisplayName("스터디 멤버 조회")
    @Test
    void view_study_members_by_managers() throws Exception {
        String testpath = getEncodedUrl("testpath");
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(testpath).get();

        mockMvc.perform(get(URL_STUDY_PATH + getEncodedUrl(testpath) + URL_STUDY_MEMBERS))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(view().name(VIEW_STUDY_MEMBERS));
    }


}