package com.musicmanager.web.controller;

import com.musicmanager.web.entity.User;
import com.musicmanager.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String getUsers(Model model) {
        List<User> users = userService.getUsers();
        model.addAttribute("users", users);
        model.addAttribute("userRequest", new User());
        return "users";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("userRequest") User user) {
        userService.createUser(user);
        return "redirect:/users";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable String id, @ModelAttribute("userRequest") User user) {
        userService.updateUser(id, user);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam("keyword") String keyword, Model model) {
        List<User> users = userService.searchUsers(keyword);
        model.addAttribute("users", users);
        model.addAttribute("userRequest", new User());
        model.addAttribute("searchQuery", keyword);
        return "users";
    }
}
