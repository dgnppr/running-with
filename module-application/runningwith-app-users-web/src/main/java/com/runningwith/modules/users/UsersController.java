package com.runningwith.modules.users;

import com.runningwith.modules.users.form.SignUpForm;
import com.runningwith.modules.users.validator.SignUpFormValidator;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static com.runningwith.infra.utils.WebUtils.REDIRECT;
import static com.runningwith.infra.utils.WebUtils.URL_REDIRECT_ROOT;

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
    public static final String URL_USERS_PROFILE = "/profile";
    public static final String PAGE_USERS_PROFILE = "users/profile";
    public static final String URL_EMAIL_LOGIN = "/email-login";
    public static final String VIEW_EMAIL_LOGIN = "users/email-login";
    public static final String URL_LOGIN_BY_EMAIL = "/login-by-email";
    public static final String VIEW_USERS_LOGGED_IN_BY_EMAIL = "users/logged-in-by-email";
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

    @GetMapping(URL_USERS_PROFILE + "/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser UsersEntity usersEntity) {
        UsersEntity entity = usersRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
        model.addAttribute("user", entity);
        model.addAttribute("isOwner", entity.equals(usersEntity));
        return PAGE_USERS_PROFILE;
    }

    @GetMapping(URL_EMAIL_LOGIN)
    public String emailLoginView() {
        return VIEW_EMAIL_LOGIN;
    }

    @PostMapping(URL_EMAIL_LOGIN)
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail(email);

        if (optionalUsersEntity.isEmpty()) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return VIEW_EMAIL_LOGIN;
        }

        UsersEntity usersEntity = optionalUsersEntity.get();

        if (!usersEntity.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 발송은 1시간에 1회 전송가능 합니다.");
            return VIEW_EMAIL_LOGIN;
        }

        usersService.sendLoginLink(usersEntity);
        attributes.addFlashAttribute("message", "이메일 인증 메일 발송 완료");
        return REDIRECT + URL_EMAIL_LOGIN;
    }

    @GetMapping(URL_LOGIN_BY_EMAIL)
    public String loginByEmail(String token, String email, Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail(email);

        if (optionalUsersEntity.isEmpty()) {
            model.addAttribute("error", "해당 링크로 로그인할 수 없습니다.");
            return VIEW_USERS_LOGGED_IN_BY_EMAIL;
        }

        UsersEntity usersEntity = optionalUsersEntity.get();

        if (!usersEntity.isValidEmailToken(token)) {
            model.addAttribute("error", "해당 링크로 로그인할 수 없습니다.");
            return VIEW_USERS_LOGGED_IN_BY_EMAIL;
        }

        usersService.login(usersEntity, request, response);
        return VIEW_USERS_LOGGED_IN_BY_EMAIL;
    }

}
