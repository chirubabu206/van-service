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

    public StudentController(StudentRepository studentRepo, UserRepository userRepo) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);
            if (currentUser != null) {
                student.setUser(currentUser);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User account context mismatch.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Please sign in first.");
        }

        // Initialize financial data tracking baselines
        student.setPendingMonths("June");
        student.setCurrentPendingFee(student.getMonthlyFee());
        student.setCurrentPaymentStatus(false);

        Student savedStudent = studentRepo.save(student);
        return ResponseEntity.ok(savedStudent);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listStudents() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);
            if (currentUser != null) {
                return ResponseEntity.ok(studentRepo.findByUserId(currentUser.getId()));
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();

        if (payload.get("studentId") == null || payload.get("pin") == null) {
            response.put("error", "Missing processing parameters.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Long studentId = Long.parseLong(payload.get("studentId"));
        String pin = payload.get("pin");

        if (!SECRET_PIN.equals(pin)) {
            response.put("error", "Incorrect 4-digit Security Code!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Student> studentOpt = studentRepo.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            student.setCurrentPaymentStatus(true);
            student.setCurrentPendingFee(0);
            student.setPendingMonths("Fully Paid"); // Changed from blank space to avoid empty loop splits later

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);
            student.setPaymentTimestamp(LocalDateTime.now().format(formatter));

            studentRepo.save(student);

            response.put("success", "Payment approved!");
            response.put("timestamp", student.getPaymentTimestamp());
            return ResponseEntity.ok(response);
        }

        response.put("error", "Student record not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/trigger-next-month")
    public ResponseEntity<String> triggerNextMonthPayments() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Student> activeUserStudents = new ArrayList<>();

        if (principal instanceof UserDetails) {
            String loggedInMobile = ((UserDetails) principal).getUsername();
            User currentUser = userRepo.findByMobileNumber(loggedInMobile).orElse(null);
            if (currentUser != null) {
                activeUserStudents = studentRepo.findByUserId(currentUser.getId());
            }
        }

        if (activeUserStudents.isEmpty()) {
            return ResponseEntity.ok("No active students found to update for this user session.");
        }

        List<String> academicMonths = Arrays.asList(
                "June", "July", "August", "September", "October",
                "November", "December", "January", "February", "March", "April"
        );

        String currentSystemMonth = "June";

        for (Student student : activeUserStudents) {
            String monthsText = student.getPendingMonths();
            if (monthsText != null && !monthsText.trim().isEmpty() && !monthsText.equals("Fully Paid")) {
                String[] split = monthsText.split(", ");
                String lastMonth = split[split.length - 1];
                if (academicMonths.contains(lastMonth) && academicMonths.indexOf(lastMonth) > academicMonths.indexOf(currentSystemMonth)) {
                    currentSystemMonth = lastMonth;
                }
            }
        }

        int currentSystemIndex = academicMonths.indexOf(currentSystemMonth);
        String nextMonthName;
        if (currentSystemIndex != -1 && currentSystemIndex < academicMonths.size() - 1) {
            nextMonthName = academicMonths.get(currentSystemIndex + 1);
        } else {
            nextMonthName = "June";
        }

        for (Student student : activeUserStudents) {
            double monthlyFee = student.getMonthlyFee();
            double currentPending = student.getCurrentPendingFee();

            if (student.isCurrentPaymentStatus() || currentPending == 0) {
                student.setCurrentPendingFee(monthlyFee);
                student.setPendingMonths(nextMonthName);
                student.setCurrentPaymentStatus(false);
            } else {
                student.setCurrentPendingFee(currentPending + monthlyFee);
                String currentMonthsText = student.getPendingMonths();
                if (currentMonthsText == null || currentMonthsText.trim().isEmpty() || currentMonthsText.equals("Fully Paid")) {
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