package com.vanservice.van_servicce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encrypts passwords securely
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabled for smoother cloud deployment configurations
                .authorizeHttpRequests(auth -> auth
                        // 🔓 ONLY allow public access to the core login/signup routes and frontend styling assets
                        .requestMatchers("/login", "/signup", "/login.html", "/signup.html", "/css/**", "/js/**").permitAll()

                        // 🔒 EVERYTHING else (adding students, months, view panels) requires a successful login session!
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // 🚪 Points Spring Security to check your native custom login form route
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login") // Processes native form POST submissions
                        .defaultSuccessUrl("/index.html", true) // ➔ Forces redirect straight to home panel on success
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                );

        return http.build();
    }
}