package com.trustbridge.Features.Auth.Controller.MVC;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @PostMapping("/register")
    public String registration() {
        return "/auth/register.html";
    }

    @PostMapping("/login")
    public String login() {
        return "/auth/login.html";
    }

}
