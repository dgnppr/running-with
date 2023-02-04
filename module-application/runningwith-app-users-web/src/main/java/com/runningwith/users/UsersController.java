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

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    private final SignUpFormValidator signUpFormValidator;
    private final UsersService usersService;

    public static final String FORM_SIGN_UP = "signUpForm";
    public static final String PAGE_SIGN_UP = "users/sign-up";
    public static final String URL_SIGN_UP = "/sign-up";

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

}
