package com.runningwith.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SettingsController {

    @GetMapping("/settings/profile")
    public String profileSetting(@CurrentUser UsersEntity usersEntity, Model model) {

        model.addAttribute("profile", usersEntity);
        model.addAttribute("nickname", usersEntity.getNickname());

        return "settings/profile";
    }
}
