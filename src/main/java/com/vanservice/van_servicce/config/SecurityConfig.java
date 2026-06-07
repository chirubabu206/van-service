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
        return new BCryptPasswordEncoder(); // Securely hashes and checks passwords using BCrypt
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabled to facilitate direct multi-user HTML form post processing
                .authorizeHttpRequests(auth -> auth
                        // 🔓 OPEN PUBLIC GATES: Added "/signup" endpoint path so registration requests bypass filters freely
                        .requestMatchers("/login.html", "/signup.html", "/signup", "/css/**", "/js/**").permitAll()

                        // 🔒 STRICT PRIVACY LOCK: Everything else (index.html dashboard, student roster lists, billing APIs) requires a session cookie
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")         // Direct frontend login interface view template
                        .loginProcessingUrl("/login")     // The processing endpoint that matches your login form action
                        .usernameParameter("mobileNumber") // Maps login form's phone number text property to standard authentication context
                        .passwordParameter("password")     // Maps password input element data
                        .defaultSuccessUrl("/index.html", true) // 🏁 Drops authenticated drivers straight onto the ledger tracking dashboard
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .invalidateHttpSession(true) // Destroys server memory reference keys instantly upon exit
                        .deleteCookies("JSESSIONID") // Deletes the local browser storage cookie file reference
                        .permitAll()
                );

        return http.build();
    }
}