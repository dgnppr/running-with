package com.runningwith.main;

import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.runningwith.utils.WebUtils.PAGE_INDEX;
import static com.runningwith.utils.WebUtils.URL_ROOT;

@Controller
public class MainController {

    public static final String URL_LOGIN = "/login";
    public static final String PAGE_LOGIN = "login";

    @GetMapping(URL_ROOT)
    public String index(@CurrentUser UsersEntity usersEntity, Model model) {
        if (usersEntity != null) {
            model.addAttribute("user", usersEntity);
        }
        return PAGE_INDEX;
    }

    @GetMapping(URL_LOGIN)
    public String login() {
        return PAGE_LOGIN;
    }
}
