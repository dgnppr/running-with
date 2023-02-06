package com.runningwith.users;

import com.runningwith.MockMvcTest;
import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
import com.runningwith.mail.EmailMessage;
import com.runningwith.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.runningwith.users.UsersController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UsersControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    AccountRepository accountRepository;
    @MockBean
    EmailService emailService;

    @DisplayName("회원가입 뷰 - 정상")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get(URL_SIGN_UP))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(FORM_SIGN_UP))
                .andExpect(view().name(PAGE_SIGN_UP))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post(URL_SIGN_UP)
                        .param("nickname", "test")
                        .param("email", "email@email.com")
                        .param("password", "goodpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail("email@email.com");
        assertThat(optionalUsersEntity.isPresent()).isTrue();
        UsersEntity usersEntity = optionalUsersEntity.get();
        assertThat(usersEntity.getPassword()).isNotEqualTo("goodpassword");
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @DisplayName("회원가입 처리 - 입력값 실패")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post(URL_SIGN_UP)
                        .param("nickname", "test")
                        .param("email", "email")
                        .param("password", "wrongpw")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(PAGE_SIGN_UP));
    }

    @DisplayName("인증 메일 확인 - 잆력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {
        AccountEntity accountEntity = new AccountEntity(AccountType.USERS);
        UsersEntity usersEntity = UsersEntity.builder()
                .accountEntity(accountEntity)
                .email("email@email.com")
                .password("goodpassword")
                .nickname("randomename")
                .build();
        usersEntity.generateEmailCheckToken();
        accountRepository.save(accountEntity);
        usersRepository.save(usersEntity);

        mockMvc.perform(get(URL_CHECK_EMAIL_TOKEN)
                        .param("token", usersEntity.getEmailCheckToken())
                        .param("email", usersEntity.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name(PAGE_CHECKED_EMAIL));
    }

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get(URL_CHECK_EMAIL_TOKEN)
                        .param("token", "wrong-token")
                        .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("nickname"))
                .andExpect(view().name(PAGE_CHECKED_EMAIL));
    }

}