package com.runningwith.users;

import com.runningwith.users.form.SignUpForm;
import com.runningwith.users.validator.SignUpFormValidator;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    public static final String FORM_SIGN_UP = "signUpForm";
    public static final String PAGE_SIGN_UP = "users/sign-up";
    public static final String PAGE_CHECKED_EMAIL = "users/checked-email";
    public static final String URL_SIGN_UP = "/sign-up";
    public static final String URL_CHECK_EMAIL_TOKEN = "/check-email-token";
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
    public String signUpSubmit(@Validated SignUpForm signUpForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return PAGE_SIGN_UP;
        }
        usersService.processNewUsers(signUpForm);
        return "redirect:/";
    }


    @GetMapping(URL_CHECK_EMAIL_TOKEN)
    public String checkEmailToken(String token, String email, Model model) {
        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail(email);

        if (optionalUsersEntity.isEmpty()) {
            model.addAttribute("error", "wrong.email");
            return PAGE_CHECKED_EMAIL;
        }

        UsersEntity usersEntity = optionalUsersEntity.get();

        if (!usersEntity.getEmailCheckToken().equals(token)) {
            model.addAttribute("error", "wrong.token");
            return PAGE_CHECKED_EMAIL;
        }

        usersService.completeSignUp(usersEntity);
        model.addAttribute("nickname", usersEntity.getNickname());
        return PAGE_CHECKED_EMAIL;

    }

}
