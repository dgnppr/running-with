package com.runningwith.domain.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.tag.TagForm;
import com.runningwith.domain.tag.TagRepository;
import com.runningwith.domain.users.form.*;
import com.runningwith.domain.zone.ZoneEntity;
import com.runningwith.domain.zone.ZoneRepository;
import com.runningwith.infra.MockMvcTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.runningwith.domain.users.SettingsController.*;
import static com.runningwith.domain.users.form.Profile.toProfile;
import static com.runningwith.infra.utils.CustomStringUtils.RANDOM_STRING;
import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.infra.utils.WebUtils.REDIRECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UsersService usersService;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ZoneRepository zoneRepository;

    private ZoneEntity testZone = ZoneEntity.builder().city("test").localNameOfCity("testcity").province("testprov").build();

    @BeforeEach
    void setUp() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void tearDown() {
        zoneRepository.deleteAll();
    }

    @WithUser
    @DisplayName("프로필 수정 뷰 테스트")
    @Test
    void view_settings_profile() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        mockMvc.perform(get(URL_SETTINGS_PROFILE))
                .andExpect(model().attribute("nickname", WITH_USER_NICKNAME))
                .andExpect(model().attribute("profile", toProfile(usersEntity)))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_PROFILE));
    }

    @WithUser
    @DisplayName("프로필 수정 - 입력 정상")
    @Test
    void update_profile_with_correct_inputs() throws Exception {

        Profile profile = new Profile("test bio", "https://test/home", "back end developer", "seoul", "");

        mockMvc.perform(post(URL_SETTINGS_PROFILE)
                        .param("bio", profile.getBio())
                        .param("profileUrl", profile.getProfileUrl())
                        .param("occupation", profile.getOccupation())
                        .param("location", profile.getLocation())
                        .param("profileImage", profile.getProfileImage())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_SETTINGS_PROFILE));

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        assertThatUsersProfileUpdated(profile, usersEntity);
    }

    @WithUser
    @DisplayName("프로필 수정 - 입력 오류")
    @Test
    void update_profile_with_wrong_inputs() throws Exception {

        String wrongStr = RANDOM_STRING;
        for (int i = 0; i < 5; i++) {
            wrongStr += RANDOM_STRING;
        }

        Profile profile = new Profile(wrongStr, wrongStr, wrongStr, wrongStr, "");

        mockMvc.perform(post(URL_SETTINGS_PROFILE)
                        .param("bio", profile.getBio())
                        .param("profileUrl", profile.getProfileUrl())
                        .param("occupation", profile.getOccupation())
                        .param("location", profile.getLocation())
                        .param("profileImage", profile.getProfileImage())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("nickname", WITH_USER_NICKNAME))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_PROFILE));
    }

    @WithUser
    @DisplayName("비밀번호 수정 뷰 테스트")
    @Test
    void view_update_password() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        mockMvc.perform(get(URL_SETTINGS_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attributeExists(PASSWORD_FORM))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_PASSWORD));
    }

    @WithUser
    @DisplayName("비밀번호 수정 - 입력값 정상")
    @Test
    void update_password_with_correct_inputs() throws Exception {

        String beforeChangedPW = usersRepository.findByNickname(WITH_USER_NICKNAME).get().getPassword();
        String correctpassword = "Correctpassword1@3";

        PasswordForm passwordForm = new PasswordForm(correctpassword, correctpassword);
        mockMvc.perform(post(URL_SETTINGS_PASSWORD)
                        .param("newPassword", passwordForm.getNewPassword())
                        .param("newPasswordConfirm", passwordForm.getNewPasswordConfirm())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_SETTINGS_PASSWORD));

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        assertThatPasswordChanged(beforeChangedPW, passwordForm, usersEntity);
    }


    @WithUser
    @DisplayName("비밀번호 수정 - 입력값 에러")
    @Test
    void update_password_with_wrong_inputs() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        PasswordForm passwordForm = new PasswordForm("1111", "1234");
        mockMvc.perform(post(URL_SETTINGS_PASSWORD)
                        .param("newPassword", passwordForm.getNewPassword())
                        .param("newPasswordConfirm", passwordForm.getNewPasswordConfirm())
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attributeExists(PASSWORD_FORM))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_PASSWORD));
    }

    @WithUser
    @DisplayName("알림 수정 뷰 테스트")
    @Test
    void update_notifications_view() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        mockMvc.perform(get(URL_SETTINGS_NOTIFICATIONS))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attributeExists(NOTIFICATIONS_FORM))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_NOTIFICATIONS));
    }

    @WithUser
    @DisplayName("알림 수정 - 입력값 정상")
    @Test
    void update_notifications() throws Exception {
        Notifications original = new Notifications(usersRepository.findByNickname(WITH_USER_NICKNAME).get());
        mockMvc.perform(post(URL_SETTINGS_NOTIFICATIONS)
                        .param("studyCreatedByEmail", "false")
                        .param("studyCreatedByWeb", "false")
                        .param("studyEnrollmentResultByEmail", "false")
                        .param("studyEnrollmentResultByWeb", "false")
                        .param("studyUpdatedByEmail", "false")
                        .param("studyUpdatedByWeb", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_SETTINGS_NOTIFICATIONS));

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        Notifications changed = new Notifications(usersEntity);

        assertThat(original).isNotEqualTo(changed);
        assertThat(changed.isStudyCreatedByEmail()).isEqualTo(false);
        assertThat(changed.isStudyCreatedByWeb()).isEqualTo(false);
        assertThat(changed.isStudyEnrollmentResultByEmail()).isEqualTo(false);
        assertThat(changed.isStudyEnrollmentResultByWeb()).isEqualTo(false);
        assertThat(changed.isStudyUpdatedByEmail()).isEqualTo(false);
        assertThat(changed.isStudyUpdatedByWeb()).isEqualTo(false);
    }

    @WithUser
    @DisplayName("계정 업데이트 뷰")
    @Test
    void view_update_users() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        mockMvc.perform(get(URL_SETTINGS_USERS))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attributeExists(NICKNAME_FORM))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_USERS));
    }

    @WithUser
    @DisplayName("닉네임 업데이트 - 입력값 정상")
    @Test
    void update_nickname_with_correct_input() throws Exception {
        String originNickname = usersRepository.findByNickname(WITH_USER_NICKNAME).get().getNickname();
        String change = "correctnickname";
        NicknameForm nicknameForm = new NicknameForm(change);
        mockMvc.perform(post(URL_SETTINGS_USERS)
                        .param("nickname", nicknameForm.getNickname())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated())
                .andExpect(view().name(REDIRECT + URL_SETTINGS_USERS));

        String changedNickname = usersRepository.findByNickname(change).get().getNickname();
        assertThat(originNickname).isNotEqualTo(changedNickname);
        assertThat(changedNickname).isEqualTo(change);
    }

    @WithUser
    @DisplayName("태그 업데이트 뷰")
    @Test
    void view_update_tag() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        List<String> userTags = usersService.getTags(usersEntity).stream().map(TagEntity::getTitle).collect(Collectors.toList());
        List<String> whitelist = tagRepository.findAll().stream().map(TagEntity::getTitle).collect(Collectors.toList());

        mockMvc.perform(get(URL_SETTINGS_TAGS))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("tags", userTags))
                .andExpect(model().attribute("whitelist", objectMapper.writeValueAsString(whitelist)))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_TAGS));
    }


    @WithUser
    @DisplayName("태그 업데이트 - 추가")
    @Test
    void update_tag_add() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(URL_SETTINGS_TAGS_ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk());

        assertThatUsersTagsAdded();
    }

    @WithUser
    @DisplayName("태그 업데이트 - 삭제")
    @Test
    void update_tag_remove() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        TagEntity tagEntity = tagRepository.save(TagEntity.builder().title("newTag").build());
        usersService.addTag(usersEntity, tagEntity);

        assertThat(usersEntity.getTags().contains(tagEntity)).isTrue();

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(URL_SETTINGS_TAGS_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk());

        assertThatUsersTagRemoved(usersEntity, tagEntity);
    }

    @WithUser
    @DisplayName("존 업데이트 뷰")
    @Test
    void view_update_zone() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        List<String> zones = usersService.getZones(usersEntity).stream().map(ZoneEntity::toString).collect(Collectors.toList());
        List<String> whitelist = zoneRepository.findAll().stream().map(ZoneEntity::toString).collect(Collectors.toList());

        mockMvc.perform(get(URL_SETTINGS_ZONES))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("zones", zones))
                .andExpect(model().attribute("whitelist", objectMapper.writeValueAsString(whitelist)))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_ZONES));
    }

    @WithUser
    @DisplayName("존 업데이트 - 추가")
    @Test
    void update_zone_add() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(URL_SETTINGS_ZONES_ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk());

        assertThatUsersZoneAdded();
    }

    @WithUser
    @DisplayName("존 업데이트 - 삭제")
    @Test
    void update_zone_remove() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(URL_SETTINGS_ZONES_REMOVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(authenticated())
                .andExpect(status().isOk());

        assertThatUsersZoneRemoved();
    }

    private void assertThatUsersZoneRemoved() {
        ZoneEntity zoneEntity = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince()).get();
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        assertThat(usersEntity.getZones().contains(zoneEntity)).isFalse();
    }

    private void assertThatUsersTagRemoved(UsersEntity usersEntity, TagEntity tagEntity) {
        assertThat(usersEntity.getTags().contains(tagEntity)).isFalse();
    }

    private void assertThatUsersZoneAdded() {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        ZoneEntity zoneEntity = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince()).get();
        assertThat(usersEntity.getZones().contains(zoneEntity)).isTrue();
    }

    private void assertThatUsersProfileUpdated(Profile profile, UsersEntity usersEntity) {
        assertThat(usersEntity.getBio()).isEqualTo(profile.getBio());
        assertThat(usersEntity.getProfileUrl()).isEqualTo(profile.getProfileUrl());
        assertThat(usersEntity.getOccupation()).isEqualTo(profile.getOccupation());
        assertThat(usersEntity.getLocation()).isEqualTo(profile.getLocation());
        assertThat(usersEntity.getProfileImage()).isEqualTo(profile.getProfileImage());
    }

    private void assertThatPasswordChanged(String beforeChangedPW, PasswordForm passwordForm, UsersEntity usersEntity) {
        assertThat(usersEntity.getPassword()).isNotEqualTo(beforeChangedPW); // check whether password is changed
        assertThat(usersEntity.getPassword()).isNotEqualTo(passwordForm.getNewPassword()); // check whether password is encoded
        assertThat(passwordEncoder.matches(passwordForm.getNewPassword(), usersEntity.getPassword())).isTrue();
    }

    private void assertThatUsersTagsAdded() {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        Optional<TagEntity> optionalTag = tagRepository.findByTitle("newTag");
        assertThat(optionalTag).isPresent();
        TagEntity tagEntity = optionalTag.get();
        assertThat(usersEntity.getTags().contains(tagEntity)).isTrue();
    }


}