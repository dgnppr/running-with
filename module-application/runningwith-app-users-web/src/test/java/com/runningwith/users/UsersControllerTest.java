package com.runningwith.users;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import com.runningwith.account.AccountRepository;
import com.runningwith.mail.EmailMessage;
import com.runningwith.mail.EmailService;
import com.runningwith.users.form.SignUpForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.runningwith.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.WithUserSecurityContextFactory.PASSWORD;
import static com.runningwith.users.UsersController.*;
import static com.runningwith.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.utils.WebUtils.URL_REDIRECT_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
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
    @Autowired
    UsersService usersService;
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
                .andExpect(view().name(URL_REDIRECT_ROOT));

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

    @WithUser
    @DisplayName("인증 메일 확인 - 잆력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        mockMvc.perform(get(URL_CHECK_EMAIL_TOKEN)
                        .param("token", usersEntity.getEmailCheckToken())
                        .param("email", usersEntity.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(authenticated())
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
                .andExpect(unauthenticated())
                .andExpect(view().name(PAGE_CHECKED_EMAIL));
    }

    @WithUser
    @DisplayName("인증 메일 뷰 - 정상")
    @Test
    void checkEmail() throws Exception {
        mockMvc.perform(get(URL_CHECK_EMAIL))
                .andExpect(model().attribute("email", WITH_USER_NICKNAME + EMAIL))
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_CHECK_EMAIL));
    }

    @WithUser
    @DisplayName("재전송 메일 - 1시간 이내 중복 요청")
    @Test
    void resendConfirmEmail() throws Exception {
        mockMvc.perform(get(URL_RESEND_CONFIRM_EMAIL))
                .andExpect(model().attribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다."))
                .andExpect(model().attribute("email", WITH_USER_NICKNAME + EMAIL))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_CHECK_EMAIL));
    }

    @WithUser
    @DisplayName("프로필 뷰 성공 - by self")
    @Test
    void view_profile_success_by_self() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        mockMvc.perform(get(URL_USERS_PROFILE + "/" + WITH_USER_NICKNAME))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("isOwner", true))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_USERS_PROFILE));
    }

    @DisplayName("프로필 뷰 성공 - by other")
    @Test
    void view_profile_success_by_other() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("nickname");
        signUpForm.setEmail("nickname" + EMAIL);
        signUpForm.setPassword(PASSWORD);
        UsersEntity usersEntity = usersService.processNewUsers(signUpForm);

        mockMvc.perform(get(URL_USERS_PROFILE + "/" + "nickname"))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("isOwner", false))
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(view().name(PAGE_USERS_PROFILE));
    }

    // TODO 프로필 뷰 실패 테스트 코드 작성
    @DisplayName("프로필 뷰 실패 - 에러 경로")
    @Test
    void view_profile_failure_wrong_path() throws Exception {
        // mockMvc.perform(get(URL_USERS_PROFILE + "/" + UUID.randomUUID()))
    }


}