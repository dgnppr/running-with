package com.runningwith.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        return "user/sign-up";
    }

}
