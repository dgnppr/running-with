package com.runningwith.users;

import com.runningwith.MockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.runningwith.users.UsersController.SIGN_UP;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UsersControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;

    @DisplayName("회원가입 뷰 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get(SIGN_UP))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(view().name("user/sign-up"))
                .andExpect(unauthenticated());
    }
}