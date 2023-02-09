package com.runningwith.users;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.users.form.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.runningwith.users.SettingsController.URL_SETTINGS_PROFILE;
import static com.runningwith.users.SettingsController.VIEW_SETTINGS_PROFILE;
import static com.runningwith.users.form.Profile.toProfile;
import static com.runningwith.utils.CustomStringUtils.RANDOM_STRING;
import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.utils.WebUtils.REDIRECT;
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

    @WithUser
    @DisplayName("프로필 수정 뷰 테스트")
    @Test
    void settings_profile_view() throws Exception {

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

        assertThat(usersEntity.getBio()).isEqualTo(profile.getBio());
        assertThat(usersEntity.getProfileUrl()).isEqualTo(profile.getProfileUrl());
        assertThat(usersEntity.getOccupation()).isEqualTo(profile.getOccupation());
        assertThat(usersEntity.getLocation()).isEqualTo(profile.getLocation());
        // TODO assert profileImage field
        // assertThat(usersEntity.getProfileImage()).isEqualTo(profile.getProfileImage());
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
                .andExpect(model().attribute("nickname", WITH_USER_NICKNAME))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_SETTINGS_PROFILE));

    }

}