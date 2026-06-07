package com.vanservice.van_servicce;

// 🔀 IMPORT LINES: Linking models and repositories cleanly across project structures
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

	/**
	 * 🏁 AUTOMATED SEED DATA RUNNER:
	 * This bean fires automatically every single time your application boots up on Render.
	 * It checks if your dad's master admin mobile account exists, and if not, it configures it.
	 */
	@Bean
	public CommandLineRunner initDefaultUser(UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			String adminMobile = "9704552622"; // Master layout access key

			// 🛡️ SECURITY TRIMMING: Safely look up the cleaned number to prevent duplicate data row skips
			if (userRepo.findByMobileNumber(adminMobile.trim()).isEmpty()) {
				User admin = new User();
				admin.setMobileNumber(adminMobile.trim());

				// Encrypts "chiru123" into a clean, standalone BCrypt signature block
				admin.setPassword(encoder.encode("chiru123"));
				admin.setRole("ADMIN"); // Gives master privileges directly to this seed profile

				userRepo.save(admin);
				System.out.println(">>> [DATABASE INITIALIZER] Success: Master admin account created cleanly.");
			} else {
				System.out.println(">>> [DATABASE INITIALIZER] Verification: Admin profile row already active in Neon.");
			}
		};
	}
}