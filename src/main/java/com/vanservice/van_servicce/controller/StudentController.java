package com.vanservice.van_servicce.controller;

import com.vanservice.van_servicce.model.Student;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*") // Allows frontend interfacing
public class StudentController {

    private final Map<Long, Student> databaseMock = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private static final String SECRET_PIN = "1234"; // Your dad's secure 4-digit code

    @PostMapping("/add")
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        Long newId = idGenerator.getAndIncrement();
        student.setId(newId);
        databaseMock.put(newId, student);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/list")
    public ResponseEntity<Collection<Student>> listStudents() {
        return ResponseEntity.ok(databaseMock.values());
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

        Student student = databaseMock.get(studentId);
        if (student != null) {
            student.setCurrentMonthPaid(true);

            // Format precise payment time stamp (e.g., "05 Jun, 09:43 PM")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);
            student.setPaymentTimestamp(LocalDateTime.now().format(formatter));

            response.put("success", "Payment approved!");
            response.put("timestamp", student.getPaymentTimestamp());
            return ResponseEntity.ok(response);
        }

        response.put("error", "Student record not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}