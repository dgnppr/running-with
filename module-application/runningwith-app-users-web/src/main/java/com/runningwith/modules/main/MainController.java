package com.runningwith.modules.main;

import com.runningwith.modules.users.CurrentUser;
import com.runningwith.modules.users.UsersEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.runningwith.infra.utils.WebUtils.URL_ROOT;
import static com.runningwith.infra.utils.WebUtils.VIEW_INDEX;

@Controller
public class MainController {

    public static final String URL_LOGIN = "/login";
    public static final String PAGE_LOGIN = "login";

    @GetMapping(URL_ROOT)
    public String index(@CurrentUser UsersEntity usersEntity, Model model) {
        if (usersEntity != null) {
            model.addAttribute("user", usersEntity);
        }
        return VIEW_INDEX;
    }

    @GetMapping(URL_LOGIN)
    public String login(@CurrentUser UsersEntity usersEntity) {
        if (usersEntity != null) {
            return VIEW_INDEX;
        }
        return PAGE_LOGIN;
    }
}
