package com.vanservice.van_servicce;

import com.vanservice.van_servicce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query method to look up a profile using their mobile login string
    Optional<User> findByMobileNumber(String mobileNumber);
}