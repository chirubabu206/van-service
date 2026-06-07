package com.vanservice.van_servicce;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // 🌐 Maps the root path to serve your dashboard template
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 🚪 Maps the secure login link route cleanly
    @GetMapping("/login.html")
    public String login() {
        return "login";
    }

    // 📝 Maps the signup registration endpoint
    @GetMapping("/signup.html")
    public String signup() {
        return "signup";
    }
}