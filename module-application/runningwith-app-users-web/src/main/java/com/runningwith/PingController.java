package com.runningwith;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PingController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
