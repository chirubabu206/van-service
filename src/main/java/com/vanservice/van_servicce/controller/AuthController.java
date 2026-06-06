package com.vanservice.van_servicce.controller;

// 🔀 IMPORT LINES: Linking your components across different folders cleanly
import com.vanservice.van_servicce.model.User;
import com.vanservice.van_servicce.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // The Constructor: This automatically pulls in your database repository and password encrypter
    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobileNumber");
        String plainPassword = payload.get("password");

        Map<String, String> response = new HashMap<>();

        // 1. Validation Check: Ensure the user didn't leave fields blank
        if (mobile == null || mobile.trim().isEmpty() || plainPassword == null || plainPassword.trim().isEmpty()) {
            response.put("error", "Mobile number and password are required fields!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. Duplication Check: Make sure a colleague doesn't register with an existing number
        if (userRepo.findByMobileNumber(mobile).isPresent()) {
            response.put("error", "This mobile number is already registered!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // 3. Secure Encryption and Storage
        User newUser = new User();
        newUser.setMobileNumber(mobile);

        // 🛡️ SECURITY CRITICAL: This line intercepts the plain text password (e.g. "myPassword123")
        // and converts it into an unreadable cryptographic string before saving it to MySQL.
        newUser.setPassword(passwordEncoder.encode(plainPassword));
        newUser.setRole("USER");

        userRepo.save(newUser);

        response.put("success", "Account created successfully! Redirecting to login...");
        return ResponseEntity.ok(response);
    }
}