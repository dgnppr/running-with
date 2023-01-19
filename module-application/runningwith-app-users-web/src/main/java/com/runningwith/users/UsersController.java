package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    public static final String SIGN_UP = "/sign-up";

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping(path = SIGN_UP)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "user/sign-up";
    }

    @PostMapping(path = SIGN_UP)
    public String signUpSubmit(@Validated SignUpForm signUpForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "user/sign-up";
        }

        UsersEntity usersEntity = UsersEntity.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword()) // TODO password encoding
                .emailCheckToken(UUID.randomUUID().toString())
                .emailCheckTokenGeneratedAt(LocalDateTime.now())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .accountEntity(new AccountEntity(AccountType.USERS))
                .build();

        accountRepository.save(usersEntity.getAccountEntity());
        usersRepository.save(usersEntity);

        return "redirect:/";
    }

}
