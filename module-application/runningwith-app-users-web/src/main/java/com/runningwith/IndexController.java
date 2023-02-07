package com.runningwith;

import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.runningwith.utils.WebUtils.URL_ROOT;

@Controller
public class IndexController {

    @GetMapping(URL_ROOT)
    public String index(@CurrentUser UsersEntity usersEntity, Model model) {
        if (usersEntity != null) {
            model.addAttribute(usersEntity);
        }
        return "index";
    }
}
