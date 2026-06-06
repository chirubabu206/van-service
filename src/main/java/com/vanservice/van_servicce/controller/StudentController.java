package com.vanservice.van_servicce.controller;

import com.vanservice.van_servicce.model.Student;
import com.vanservice.van_servicce.StudentRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {

    private final StudentRepository studentRepo;
    private static final String SECRET_PIN = "1234"; // Your dad's secure 4-digit code

    // Constructor Injection — clears the "Field injection is not recommended" warning completely!
    public StudentController(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    @PostMapping("/add")
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        // 🛡️ SECURITY STEP: Manually lock in the initial academic year metrics
        student.setPendingMonths("June");
        student.setCurrentPendingFee(student.getMonthlyFee());
        student.setCurrentPaymentStatus(false); // They start out owing June's fee

        Student savedStudent = studentRepo.save(student);
        return ResponseEntity.ok(savedStudent);
    }

    @GetMapping("/list")
    public ResponseEntity<Collection<Student>> listStudents() {
        // Pulls directly from your local MySQL van_db tables
        return ResponseEntity.ok(studentRepo.findAll());
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

        // Fetch directly from your database
        Optional<Student> studentOpt = studentRepo.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            // Matched precisely with your Student.java variable name
            student.setCurrentPaymentStatus(true);

            // FIX: Reset financial balance back to zero since they just settled their dues!
            student.setCurrentPendingFee(0);

            student.setPendingMonths(""); // Clear pending months text list

            // Format precise payment timestamp (e.g., "06 Jun, 12:10 PM")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);
            student.setPaymentTimestamp(LocalDateTime.now().format(formatter));

            studentRepo.save(student); // Saves modifications straight back to MySQL

            response.put("success", "Payment approved!");
            response.put("timestamp", student.getPaymentTimestamp());
            return ResponseEntity.ok(response);
        }

        response.put("error", "Student record not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Keep track of the system's current operating month at the top of the loop logic
    @PostMapping("/trigger-next-month")
    public ResponseEntity<String> triggerNextMonthPayments() {
        List<Student> allStudents = studentRepo.findAll();

        // 1. Define our ordered sequence of months for the academic van year
        List<String> academicMonths = Arrays.asList(
                "June", "July", "August", "September", "October",
                "November", "December", "January", "February", "March", "April"
        );

        // 2. Figure out what the current month is by looking at who is already in the system
        String currentSystemMonth = "June"; // Default fallback start

        for (Student student : allStudents) {
            String monthsText = student.getPendingMonths();
            if (monthsText != null && !monthsText.trim().isEmpty()) {
                String[] split = monthsText.split(", ");
                String lastMonth = split[split.length - 1];
                // Find the highest advanced month currently running in our database records
                if (academicMonths.indexOf(lastMonth) > academicMonths.indexOf(currentSystemMonth)) {
                    currentSystemMonth = lastMonth;
                }
            }
        }

        // 3. Determine the absolute next billing month across the board
        int currentSystemIndex = academicMonths.indexOf(currentSystemMonth);
        String nextMonthName;
        if (currentSystemIndex != -1 && currentSystemIndex < academicMonths.size() - 1) {
            nextMonthName = academicMonths.get(currentSystemIndex + 1); // e.g., "June" -> "July", "July" -> "August"
        } else {
            nextMonthName = "June"; // Reset safety loop back to start if out of bounds
        }

        // 4. Update every student based on this unified target month
        for (Student student : allStudents) {
            double monthlyFee = student.getMonthlyFee();
            double currentPending = student.getCurrentPendingFee();

            if (currentPending == 0) {
                // Case A: Student was fully paid up!
                // They start the upcoming system month fresh with a single month's fee.
                student.setCurrentPendingFee(monthlyFee);
                student.setPendingMonths(nextMonthName);
                student.setCurrentPaymentStatus(false);
            } else {
                // Case B: They already owe money! Stacks debt + appends the new month
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

        studentRepo.saveAll(allStudents);
        return ResponseEntity.ok("Monthly billing cycles updated successfully locally!");
    }
}