package com.vanservice.van_servicce;

// 🔀 IMPORT LINES: Tells Java exactly where to look for your models and repositories
import com.vanservice.van_servicce.model.User;
import com.vanservice.van_servicce.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class VanServicceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VanServicceApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDefaultUser(UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			String adminMobile = "9704552622"; // Master login account

			// Checks your repository safely now
			if (userRepo.findByMobileNumber(adminMobile).isEmpty()) {
				User admin = new User();
				admin.setMobileNumber(adminMobile);
				admin.setPassword(encoder.encode("chiru123"));
				admin.setRole("ADMIN");

				userRepo.save(admin);
				System.out.println(">>> Security Alert: Default admin profile initialized successfully in database!");
			}
		};
	}
}