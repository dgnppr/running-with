package com.runningwith.domain.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningwith.domain.study.event.StudyCreatedEvent;
import com.runningwith.domain.study.event.StudyEventListener;
import com.runningwith.domain.study.event.StudyUpdatedEvent;
import com.runningwith.domain.study.factory.StudyEntityFactory;
import com.runningwith.domain.study.form.StudyDescriptionForm;
import com.runningwith.domain.study.form.StudyForm;
import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.tag.TagForm;
import com.runningwith.domain.tag.TagRepository;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.WithUser;
import com.runningwith.domain.users.factory.UsersEntityFactory;
import com.runningwith.domain.users.form.ZoneForm;
import com.runningwith.domain.zone.ZoneEntity;
import com.runningwith.domain.zone.ZoneRepository;
import com.runningwith.infra.MockMvcTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static com.runningwith.domain.AppExceptionHandler.VIEW_ERROR;
import static com.runningwith.domain.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.domain.study.StudySettingsController.*;
import static com.runningwith.infra.utils.CustomStringUtils.*;
import static com.runningwith.infra.utils.WebUtils.REDIRECT;
import static com.runningwith.infra.utils.WebUtils.URL_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest {

    public static final String TEST_PATH = "testpath";
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    StudyService studyService;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    StudyEntityFactory studyEntityFactory;
    @Autowired
    UsersEntityFactory usersEntityFactory;
    @MockBean
    StudyEventListener studyEventListener;

    @BeforeEach
    void setUp() {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyForm studyForm = studyEntityFactory.createStudyForm(TEST_PATH, "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyEntityFactory.createStudyEntity(usersEntity, studyEntity);
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @WithUser
    @DisplayName("스터디 소개글 수정 뷰 - by manager")
    @Test
    void view_study_setting_description_by_managers() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_DESCRIPTION))
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
        UsersEntity other = usersEntityFactory.createUsersEntity("nickname");
        StudyForm studyForm = studyEntityFactory.createStudyForm("ttestpath", "testpath", "testpath", "testpath");
        StudyEntity studyEntity = studyForm.toEntity();
        studyEntityFactory.createStudyEntity(other, studyEntity);

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
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();

        StudyDescriptionForm form = new StudyDescriptionForm("test", "test");

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_DESCRIPTION)
                        .param("shortDescription", form.getShortDescription())
                        .param("fullDescription", form.getFullDescription())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_STUDY_PATH + getEncodedUrl(studyEntity.getPath()) + URL_STUDY_SETTINGS_DESCRIPTION));

        assertThatStudyDescriptionUpdated(form);
    }

    @WithUser
    @DisplayName("스터디 소개글 업데이트 - 입력값 오류")
    @Test
    void update_study_setting_description_with_wrong_inputs() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        StudyDescriptionForm form = new StudyDescriptionForm("", "");

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_DESCRIPTION)
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
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();

        mockMvc.perform(get(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_BANNER))
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

        String origin = studyRepository.findByPath(TEST_PATH).get().getBannerImage();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_BANNER)
                        .param("image", RANDOM_STRING)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(flash().attribute("message", "배너 이미지 수정 완료"))
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTINGS_BANNER));

        assertThatBannerImageChanged(origin);
    }

    @WithUser
    @DisplayName("배너 이미지 사용 업데이트")
    @Test
    void enable_study_banner_image() throws Exception {
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_BANNER_ENABLE)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTINGS_BANNER));

        assertThat(studyEntity.isUseBanner()).isEqualTo(true);
    }

    @WithUser
    @DisplayName("배너 이미지 미사용 업데이트")
    @Test
    void disable_study_banner_image() throws Exception {
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_BANNER_DISABLE)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTINGS_BANNER));

        assertThat(studyEntity.isUseBanner()).isEqualTo(false);
    }

    @WithUser
    @DisplayName("스터디 주제 업데이트 뷰")
    @Test
    void view_study_tags_update() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        List<String> tags = studyService.getStudyTags(studyEntity).stream().map(TagEntity::getTitle).collect(Collectors.toList());
        List<String> whitelist = tagRepository.findAll().stream().map(TagEntity::getTitle).collect(Collectors.toList());

        mockMvc.perform(get(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_TAGS))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("tags", tags))
                .andExpect(model().attribute("whitelist", objectMapper.writeValueAsString(whitelist)))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_STUDY_SETTINGS_TAGS));
    }

    @WithUser
    @DisplayName("스터디 주제 추가")
    @Test
    void add_study_tags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("TEST_TAG_TITLE");

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_TAGS_ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        assertThatStudyTagsAdded(tagForm);
    }

    @WithUser
    @DisplayName("스터디 주제 삭제 - 입력값 정상")
    @Test
    void remove_study_tags_with_correct_inputs() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("TEST_TAG_TITLE");
        TagEntity tagEntity = TagEntity.builder().title(tagForm.getTagTitle()).build();
        tagRepository.save(tagEntity);
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        usersEntity.getTags().add(tagEntity);

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_TAGS_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        assertThatStudyTagsRemoved(tagEntity);
    }

    @WithUser
    @DisplayName("스터디 주제 삭제 - 입력값 오류(존재하지 않는 주제)")
    @Test
    void remove_study_tags_with_wrong_inputs() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("TEST_TAG_TITLE");

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_TAGS_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(authenticated());
    }

    @WithUser
    @DisplayName("스터디 장소 업데이트 뷰")
    @Test
    void view_study_zones_update() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        List<String> zones = studyService.getStudyTags(studyEntity).stream().map(TagEntity::getTitle).collect(Collectors.toList());
        List<String> allZones = zoneRepository.findAll().stream().map(ZoneEntity::toString).collect(Collectors.toList());

        mockMvc.perform(get(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_ZONES))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("zones", zones))
                .andExpect(model().attribute("whitelist", objectMapper.writeValueAsString(allZones)))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_STUDY_SETTINGS_ZONES));
    }


    @WithUser
    @DisplayName("스터디 장소 추가")
    @Test
    void add_study_zones() throws Exception {
        ZoneEntity entity = new ZoneEntity("TEST_CITY", "TEST_LOCAL", "TEST_PROVINCE");
        zoneRepository.save(entity);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(entity.toString());

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_ZONES_ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        assertThatStudyZonesAdded(zoneForm);
    }


    @WithUser
    @DisplayName("스터디 장소 삭제 - 입력값 정상")
    @Test
    void remove_study_zones_with_correct_inputs() throws Exception {
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        ZoneEntity entity = new ZoneEntity("TEST_CITY", "TEST_LOCAL", "TEST_PROVINCE");
        zoneRepository.save(entity);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(entity.toString());
        studyEntity.getZones().add(entity);

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_ZONES_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        assertThatStudyZonesRemoved(studyEntity, zoneForm);
    }


    @WithUser
    @DisplayName("스터디 장소 삭제 - 입력값 오류(존재하지 않는 장소)")
    @Test
    void remove_study_zones_with_wrong_inputs() throws Exception {
        ZoneEntity entity = new ZoneEntity("TEST_CITY", "TEST_LOCAL", "TEST_PROVINCE");
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(entity.toString());

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING_ZONES_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(authenticated());
    }

    @WithUser
    @DisplayName("스터디 상태 뷰")
    @Test
    void view_study_settings_status() throws Exception {
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        mockMvc.perform(get(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTING))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(view().name(VIEW_STUDY_SETTINGS_STUDY));
    }

    @WithUser
    @DisplayName("스터디 상태 공개로 업데이트 - 정상")
    @Test
    void publish_study() throws Exception {
        StudyEntity beforeStudyEntity = studyRepository.findByPath(TEST_PATH).get();
        boolean prevStatus = beforeStudyEntity.isPublished();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_PUBLISH)
                        .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTING));

        StudyEntity afterStudyEntity = studyRepository.findByPath(TEST_PATH).get();
        assertThatStudyPublished(prevStatus, afterStudyEntity);
        then(studyEventListener).should().handleStudyCreatedEvent(any(StudyCreatedEvent.class));
    }

    private void assertThatStudyPublished(boolean prevStatus, StudyEntity afterStudyEntity) {
        boolean afterStatus = afterStudyEntity.isPublished();

        assertThat(prevStatus).isFalse();
        assertThat(afterStatus).isTrue();
    }

    @WithUser
    @DisplayName("스터디 상태 공개로 업데이트 - 오류(종료 or 이미 공개인 상태)")
    @Test
    void plush_study_with_wrong_status() throws Exception {
        StudyEntity beforeStudyEntity = studyRepository.findByPath(TEST_PATH).get();
        beforeStudyEntity.publish();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_PUBLISH)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(IllegalArgumentException.class, ex.getClass());
                    assertEquals(ex.getMessage(), "스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
                })
                .andExpect(view().name(VIEW_ERROR));
    }

    @WithUser
    @DisplayName("스터디 상태 종료로 업데이트 - 정상")
    @Test
    void close_study() throws Exception {
        StudyEntity beforeStudyEntity = studyRepository.findByPath(TEST_PATH).get();
        beforeStudyEntity.publish();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_CLOSE)
                        .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTING));

        assertThatStudyClosed();
        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @WithUser
    @DisplayName("스터디 상태 종료로 업데이트 - 오류(공개X or 이미 종료인 상태)")
    @Test
    void close_study_with_wrong_status() throws Exception {
        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_CLOSE)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(IllegalArgumentException.class, ex.getClass());
                    assertEquals(ex.getMessage(), "스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
                })
                .andExpect(view().name(VIEW_ERROR));
    }

    @WithUser
    @DisplayName("스터디 멤버 모집 시작 - 정상")
    @Test
    void start_study_recruit() throws Exception {
        StudyEntity beforeStudy = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();
        beforeStudy.publish();

        assertThat(beforeStudy.isRecruiting()).isFalse();
        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_RECRUIT_START)
                        .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTING));

        StudyEntity afterStudy = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();
        assertThat(afterStudy.isRecruiting()).isTrue();
        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @WithUser
    @DisplayName("스터디 멤버 모집 종료 - 정상")
    @Test
    void stop_study_recruit() throws Exception {
        StudyEntity beforeStudy = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();
        beforeStudy.publish();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_RECRUIT_STOP)
                        .with(csrf()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTING));

        StudyEntity afterStudy = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();
        assertThat(afterStudy.isRecruiting()).isFalse();
        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }


    @WithUser
    @DisplayName("스터디 경로 수정 - 입력값 정상")
    @Test
    void update_study_path_with_correct_input() throws Exception {

        String newPath = "newpath";

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_PATH)
                        .param("newPath", newPath)
                        .with(csrf()))
                .andExpect(flash().attribute("message", "스터디 URL 수정 완료"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(newPath) + URL_STUDY_SETTING));

        assertThatStudyNotPresent(TEST_PATH);
        assertThatStudyPathUpdated(newPath);
    }

    private void assertThatStudyPathUpdated(String newPath) {
        assertThat(studyRepository.findStudyEntityWithManagersByPath(newPath).isPresent()).isTrue();
    }

    @WithUser
    @DisplayName("스터디 경로 수정 - 입력값 오류")
    @Test
    void update_study_path_with_wrong_input() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();

        String wrongPath = "NEWPATH";
        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_PATH)
                        .param("newPath", wrongPath)
                        .with(csrf()))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attributeExists("studyPathError"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_STUDY_SETTINGS_STUDY));

        assertThatStudyIsPresent();
        assertThatStudyNotPresent(wrongPath);
    }

    @WithUser
    @DisplayName("스터디 제목 수정 - 입력값 정상")
    @Test
    void update_study_title_with_correct_input() throws Exception {

        String newTitle = "new title";

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_TITLE)
                        .param("newTitle", newTitle)
                        .with(csrf()))
                .andExpect(flash().attribute("message", "스터디 제목 수정 완료"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_STUDY_PATH + getEncodedUrl(TEST_PATH) + URL_STUDY_SETTING));
    }

    @WithUser
    @DisplayName("스터디 제목 수정 - 입력값 오류")
    @Test
    void update_study_title_with_wrong_input() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        StudyEntity studyEntity = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();

        String wrongTitle = "wrong title";
        for (int i = 0; i < 10; i++) {
            wrongTitle += wrongTitle;
        }

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_TITLE)
                        .param("newTitle", wrongTitle)
                        .with(csrf()))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("study", studyEntity))
                .andExpect(model().attributeExists("studyTitleError"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_STUDY_SETTINGS_STUDY));
    }

    @WithUser
    @DisplayName("스터디 삭제 완료")
    @Test
    void remove_study() throws Exception {
        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_REMOVE)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(redirectedUrl(URL_ROOT));

        assertThatStudyNotPresent(TEST_PATH);
    }


    @WithUser
    @DisplayName("스터디 삭제 실패 - 공개된 스터디")
    @Test
    void remove_study_published() throws Exception {
        StudyEntity studyEntity = studyRepository.findStudyEntityWithManagersByPath(TEST_PATH).get();
        studyEntity.publish();

        mockMvc.perform(post(URL_STUDY_PATH + TEST_PATH + URL_STUDY_SETTINGS_REMOVE)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(IllegalArgumentException.class, ex.getClass());
                    assertEquals(ex.getMessage(), "스터디를 삭제할 수 없습니다.");
                })
                .andExpect(view().name(VIEW_ERROR));

        assertThatStudyIsPresent();
    }


    // TODO 스터디 삭제 로직 추가

    private void assertThatStudyIsPresent() {
        assertThatStudyPathUpdated(TEST_PATH);
    }

    private void assertThatStudyNotPresent(String wrongPath) {
        assertThat(studyRepository.findStudyEntityWithManagersByPath(wrongPath).isEmpty()).isTrue();
    }

    private void assertThatStudyClosed() {
        StudyEntity afterStudyEntity = studyRepository.findByPath(TEST_PATH).get();
        assertThat(afterStudyEntity.isClosed()).isTrue();
    }

    private void assertThatBannerImageChanged(String origin) {
        String changed = studyRepository.findByPath(TEST_PATH).get().getBannerImage();

        assertThat(changed).isNotEqualTo(origin);
    }

    private void assertThatStudyTagsAdded(TagForm tagForm) {
        TagEntity tagEntity = tagRepository.findByTitle(tagForm.getTagTitle()).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        assertThat(studyEntity.getTags()).containsOnly(tagEntity);
    }

    private void assertThatStudyTagsRemoved(TagEntity tagEntity) {
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        assertThat(studyEntity.getTags()).doesNotContain(tagEntity);
    }

    private void assertThatStudyZonesAdded(ZoneForm zoneForm) {
        ZoneEntity zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName()).get();
        StudyEntity studyEntity = studyRepository.findByPath(TEST_PATH).get();
        assertThat(studyEntity.getZones()).containsOnly(zone);
    }

    private void assertThatStudyZonesRemoved(StudyEntity studyEntity, ZoneForm zoneForm) {
        ZoneEntity zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName()).get();
        assertThat(studyEntity.getZones()).doesNotContain(zone);
    }

    private void assertThatStudyDescriptionUpdated(StudyDescriptionForm form) {
        StudyEntity study = studyRepository.findByPath(TEST_PATH).get();
        assertThat(study.getShortDescription()).isEqualTo(form.getShortDescription());
        assertThat(study.getFullDescription()).isEqualTo(form.getFullDescription());
    }
}