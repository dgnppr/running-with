package com.runningwith.main;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.runningwith.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.WithUserSecurityContextFactory.PASSWORD;
import static com.runningwith.main.MainController.URL_LOGIN;
import static com.runningwith.utils.CustomStringUtils.RANDOM_STRING;
import static com.runningwith.utils.WebUtils.PAGE_INDEX;
import static com.runningwith.utils.WebUtils.URL_ROOT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @WithUser(RANDOM_STRING)
    @DisplayName("인덱스 뷰 - 인증 유저")
    @Test
    void index_with_authenticated_user() throws Exception {
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usersEntity"))
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_INDEX));
    }

    @DisplayName("인덱스 뷰 - 익명 유저")
    @Test
    void index_with_unauthenticated_user() throws Exception {
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("usersEntity"))
                .andExpect(unauthenticated())
                .andExpect(view().name(PAGE_INDEX));
    }

    @WithUser(RANDOM_STRING)
    @DisplayName("로그인 성공 - 이메일")
    @Test
    void login_with_email() throws Exception {
        mockMvc.perform(post(URL_LOGIN)
                        .param("username", RANDOM_STRING + EMAIL)
                        .param("password", PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_ROOT))
                .andExpect(authenticated().withUsername(RANDOM_STRING));


    }

}