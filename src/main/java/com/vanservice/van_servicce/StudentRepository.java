package com.vanservice.van_servicce;

import com.vanservice.van_servicce.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 🔍 Dynamic Query Method:
     * Spring Data JPA automatically parses this method name to generate a SQL statement:
     * "SELECT * FROM students WHERE user_id = ?"
     * * This ensures that when a specific driver logs in, they can only view, update,
     * or trigger billing cycles for their own students.
     */
    List<Student> findByUserId(Long userId);
}