package com.musicmanager.web.controller;

import com.musicmanager.web.entity.User;
import com.musicmanager.web.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userRequest", new User());
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(User user) {
        if (!userService.register(user)) {
            return "redirect:/register?error";
        }
        return "redirect:/login?registered";
    }
}
