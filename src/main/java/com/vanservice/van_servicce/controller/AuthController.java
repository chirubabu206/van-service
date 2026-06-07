package com.vanservice.van_servicce.controller;

import com.vanservice.van_servicce.model.User;
import com.vanservice.van_servicce.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller; // 🚨 CHANGED: Standard Controller to handle redirects cleanly
import org.springframework.web.bind.annotation.*;

@Controller // 🚨 Changed from @RestController to support HTML form template handling
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // Dependency injection via constructor
    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // 🌐 FIXED: Maps EXACTLY to action="/signup" in your signup.html form tag
    @PostMapping("/signup")
    public String registerUser(@RequestParam("mobileNumber") String mobile,
                               @RequestParam("password") String plainPassword) {

        // 1. Validation Check: Ensure data isn't blank
        if (mobile == null || mobile.trim().isEmpty() || plainPassword == null || plainPassword.trim().isEmpty()) {
            return "redirect:/signup.html?error=invalid";
        }

        // 2. Duplication Check: Prevent identical numbers from writing to Neon database
        if (userRepo.findByMobileNumber(mobile.trim()).isPresent()) {
            return "redirect:/signup.html?error=exists";
        }

        // 3. Build entity and safely apply BCrypt encoding
        User newUser = new User();
        newUser.setMobileNumber(mobile.trim());
        newUser.setPassword(passwordEncoder.encode(plainPassword.trim())); // Secure hashing filter
        newUser.setRole("USER");

        // 4. Save and commit straight into the active row grid in Neon
        userRepo.save(newUser);

        // 🏁 SUCCESS: Tell the browser to redirect back to login, appending a success flag
        return "redirect:/login.html?success";
    }
}