package com.runningwith.users;

import com.runningwith.users.form.SignUpForm;
import com.runningwith.users.validator.SignUpFormValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

import static com.runningwith.utils.WebUtils.URL_REDIRECT_ROOT;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    public static final String FORM_SIGN_UP = "signUpForm";
    public static final String PAGE_SIGN_UP = "users/sign-up";
    public static final String PAGE_CHECKED_EMAIL = "users/checked-email";
    public static final String URL_SIGN_UP = "/sign-up";
    public static final String URL_CHECK_EMAIL_TOKEN = "/check-email-token";
    public static final String URL_CHECK_EMAIL = "/check-email";
    public static final String PAGE_CHECK_EMAIL = "users/check-email";
    public static final String URL_RESEND_CONFIRM_EMAIL = "/resend-confirm-email";
    private final SignUpFormValidator signUpFormValidator;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @InitBinder(FORM_SIGN_UP)
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping(path = URL_SIGN_UP)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return PAGE_SIGN_UP;
    }

    @PostMapping(path = URL_SIGN_UP)
    public String signUpSubmit(@Validated SignUpForm signUpForm, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return PAGE_SIGN_UP;
        }
        UsersEntity newUsersEntity = usersService.processNewUsers(signUpForm);
        usersService.login(newUsersEntity, request, response);
        return URL_REDIRECT_ROOT;
    }


    @GetMapping(URL_CHECK_EMAIL_TOKEN)
    public String checkEmailToken(String token, String email, Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail(email);

        if (optionalUsersEntity.isEmpty()) {
            model.addAttribute("error", "wrong.email");
            return PAGE_CHECKED_EMAIL;
        }

        UsersEntity usersEntity = optionalUsersEntity.get();

        if (!usersEntity.isValidEmailToken(token)) {
            model.addAttribute("error", "wrong.token");
            return PAGE_CHECKED_EMAIL;
        }

        usersService.completeSignUp(usersEntity);
        usersService.login(usersEntity, request, response);
        model.addAttribute("nickname", usersEntity.getNickname());
        return PAGE_CHECKED_EMAIL;
    }

    @GetMapping(URL_CHECK_EMAIL)
    public String checkEmail(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("email", usersEntity.getEmail());
        return PAGE_CHECK_EMAIL;
    }

    @GetMapping(URL_RESEND_CONFIRM_EMAIL)
    public String resendConfirmEmail(@CurrentUser UsersEntity usersEntity, Model model) {
        if (!usersEntity.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", usersEntity.getEmail());
            return PAGE_CHECK_EMAIL;
        }

        usersService.sendSignUpConfirmEmail(usersEntity);
        return URL_REDIRECT_ROOT;
    }

}
