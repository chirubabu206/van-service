package com.vanservice.van_servicce.controller;

import com.vanservice.van_servicce.model.Student;
import com.vanservice.van_servicce.model.User;
import com.vanservice.van_servicce.StudentRepository;
import com.vanservice.van_servicce.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {

    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private static final String SECRET_PIN = "1234"; // Your dad's secure 4-digit code

    // Constructor Injection — clears the field injection warnings and pulls user database context cleanly
    public StudentController(StudentRepository studentRepo, UserRepository userRepo) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        // 👤 SECURITY STEP: Identify who is logged in right now via session cookies
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);
            if (currentUser != null) {
                // 📌 Bind this student explicitly to the logged-in driver/user account entity
                student.setUser(currentUser);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User account context mismatch.");
            }
        } else {
            // ❌ Stop the insert query entirely if a guest tries to bypass the page and hit the API endpoint
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Please sign in first.");
        }

        // 🛡️ FINANCIAL INITIALIZATION: Manually lock in the initial academic year metrics
        student.setPendingMonths("June");
        student.setCurrentPendingFee(student.getMonthlyFee());
        student.setCurrentPaymentStatus(false); // They start out owing June's fee

        Student savedStudent = studentRepo.save(student);
        return ResponseEntity.ok(savedStudent);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listStudents() {
        // 👤 IDENTITY STEP: Identify the current logged-in session user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);
            if (currentUser != null) {
                // 🔍 Isolation Filter: Return ONLY the students created by this specific user account ID
                return ResponseEntity.ok(studentRepo.findByUserId(currentUser.getId()));
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, String> payload) {
        Long studentId = Long.parseLong(payload.get("studentId"));
        String pin = payload.get("pin");

        Map<String, String> response = new HashMap<>();

        if (!SECRET_PIN.equals(pin)) {
            response.put("error", "Incorrect 4-digit Security Code!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Fetch directly from your cloud database
        Optional<Student> studentOpt = studentRepo.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            student.setCurrentPaymentStatus(true);
            student.setCurrentPendingFee(0); // Reset financial balance back to zero since they just paid
            student.setPendingMonths(""); // Clear pending months text list

            // Format precise payment timestamp (e.g., "06 Jun, 12:10 PM")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);
            student.setPaymentTimestamp(LocalDateTime.now().format(formatter));

            studentRepo.save(student); // Saves modifications straight back to cloud PostgreSQL

            response.put("success", "Payment approved!");
            response.put("timestamp", student.getPaymentTimestamp());
            return ResponseEntity.ok(response);
        }

        response.put("error", "Student record not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/trigger-next-month")
    public ResponseEntity<String> triggerNextMonthPayments() {
        // Fetch only students belonging to the logged-in user to avoid shifting months for other drivers' grids
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Student> activeUserStudents = new ArrayList<>();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);            if (currentUser != null) {
                activeUserStudents = studentRepo.findByUserId(currentUser.getId());
            }
        }

        if (activeUserStudents.isEmpty()) {
            return ResponseEntity.ok("No active students found to update for this user session.");
        }

        // 1. Define our ordered sequence of months for the academic van year
        List<String> academicMonths = Arrays.asList(
                "June", "July", "August", "September", "October",
                "November", "December", "January", "February", "March", "April"
        );

        // 2. Figure out what the current month is by looking at who is already in the system
        String currentSystemMonth = "June";

        for (Student student : activeUserStudents) {
            String monthsText = student.getPendingMonths();
            if (monthsText != null && !monthsText.trim().isEmpty()) {
                String[] split = monthsText.split(", ");
                String lastMonth = split[split.length - 1];
                if (academicMonths.indexOf(lastMonth) > academicMonths.indexOf(currentSystemMonth)) {
                    currentSystemMonth = lastMonth;
                }
            }
        }

        // 3. Determine the absolute next billing month across the board
        int currentSystemIndex = academicMonths.indexOf(currentSystemMonth);
        String nextMonthName;
        if (currentSystemIndex != -1 && currentSystemIndex < academicMonths.size() - 1) {
            nextMonthName = academicMonths.get(currentSystemIndex + 1);
        } else {
            nextMonthName = "June";
        }

        // 4. Update every student belonging to this specific user session
        for (Student student : activeUserStudents) {
            double monthlyFee = student.getMonthlyFee();
            double currentPending = student.getCurrentPendingFee();

            if (currentPending == 0) {
                student.setCurrentPendingFee(monthlyFee);
                student.setPendingMonths(nextMonthName);
                student.setCurrentPaymentStatus(false);
            } else {
                student.setCurrentPendingFee(currentPending + monthlyFee);
                String currentMonthsText = student.getPendingMonths();
                if (currentMonthsText == null || currentMonthsText.trim().isEmpty()) {
                    student.setPendingMonths(nextMonthName);
                } else {
                    student.setPendingMonths(currentMonthsText + ", " + nextMonthName);
                }
                student.setCurrentPaymentStatus(false);
            }
        }

        studentRepo.saveAll(activeUserStudents);
        return ResponseEntity.ok("Monthly billing cycles updated successfully!");
    }
}