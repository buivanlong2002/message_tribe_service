package com.example.message_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("api/login")
    public String loginPage() {
        return "/auth/login";
    }

    @GetMapping("api/register")
    public String registerPage() {
        return "/auth/register";
    }

    @GetMapping("api/profile")
    public String profilePage() {
        return "profile";
    }
    @GetMapping("api/index")
    public String indexPage() {
        return "/index";
    }
}
