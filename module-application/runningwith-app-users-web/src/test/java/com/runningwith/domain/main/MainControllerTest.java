package com.runningwith.domain.main;

import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.UsersService;
import com.runningwith.domain.users.WithUser;
import com.runningwith.domain.users.form.SignUpForm;
import com.runningwith.infra.MockMvcTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.runningwith.domain.main.MainController.URL_LOGIN;
import static com.runningwith.domain.main.MainController.VIEW_INDEX_AFTER_LOGIN;
import static com.runningwith.domain.users.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.domain.users.WithUserSecurityContextFactory.PASSWORD;
import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.infra.utils.WebUtils.URL_ROOT;
import static com.runningwith.infra.utils.WebUtils.VIEW_INDEX;
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

    @Autowired
    UsersService usersService;

    @Autowired
    UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("nickname");
        signUpForm.setEmail("nickname" + EMAIL);
        signUpForm.setPassword(PASSWORD);
        usersService.processNewUsers(signUpForm);
    }

    @AfterEach
    void afterAll() {
        usersRepository.deleteAll();
    }

    // TODO add model expect after login
    @WithUser
    @DisplayName("인덱스 뷰 - 인증 유저")
    @Test
    void index_with_authenticated_user() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(authenticated())
                .andExpect(view().name(VIEW_INDEX_AFTER_LOGIN));
    }

    @DisplayName("인덱스 뷰 - 익명 유저")
    @Test
    void index_with_unauthenticated_user() throws Exception {
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("usersEntity"))
                .andExpect(unauthenticated())
                .andExpect(view().name(VIEW_INDEX));
    }

    @DisplayName("로그인 성공 - 이메일")
    @Test
    void login_with_email() throws Exception {
        mockMvc.perform(post(URL_LOGIN)
                        .param("username", "nickname" + EMAIL)
                        .param("password", PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_ROOT))
                .andExpect(authenticated().withUsername("nickname"));
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(post(URL_LOGIN)
                        .param("username", UUID.randomUUID().toString())
                        .param("password", "wrongpass")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_LOGIN + "?error"))
                .andExpect(unauthenticated());
    }

    @WithMockUser
    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    // TODO study search result view
    // TODO index study result model

}