// Change this line at the very top of StudentRepository.java
package com.vanservice.van_servicce;

import com.vanservice.van_servicce.model.Student; // Make sure this matches your Student path
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}