package com.vanservice.van_servicce.service;

import com.vanservice.van_servicce.model.User;
import com.vanservice.van_servicce.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
        // Search our custom MySQL users table for this mobile number input
        User user = userRepo.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with mobile number: " + mobileNumber));

        // Return a standard Spring Security User object with permissions locked in
        return new org.springframework.security.core.userdetails.User(
                user.getMobileNumber(),
                user.getPassword(), // The encrypted password hash string from MySQL
                new ArrayList<>()   // Empty array list handles default clean role configurations
        );
    }
}