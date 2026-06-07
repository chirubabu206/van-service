package com.vanservice.van_servicce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Open public access to the login/signup html pages and static assets
                        .requestMatchers("/login.html", "/signup.html", "/css/**", "/js/**").permitAll()

                        // 🔒 Everything else (like index.html, dashboard API routes) requires logging in
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")         // Direct fallback link
                        .loginProcessingUrl("/login")     // The POST URL from your HTML form action
                        .usernameParameter("mobileNumber")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/index.html", true) // 🏁 Once logged in successfully, force redirect to dashboard file
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}