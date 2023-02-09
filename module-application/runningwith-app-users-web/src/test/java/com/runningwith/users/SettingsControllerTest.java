package com.runningwith.users;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

        mockMvc.perform(get("/settings/profile"))
                .andExpect(model().attribute("nickname", WITH_USER_NICKNAME))
                .andExpect(model().attribute("profile", usersEntity))
                .andExpect(authenticated())
                .andExpect(view().name("settings/profile"));
    }

}