package com.vanservice.van_servicce.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String pickupLocation;
    private Double monthlyFee;
    private Integer dueDay; // Value from 1 to 31 saved during registration

    // Tracking active current month status
    private String currentPendingMonth = "June";
    private boolean isCurrentMonthPaid = false;
    private String paymentTimestamp;

    // Default Constructor
    public Student() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public Double getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(Double monthlyFee) { this.monthlyFee = monthlyFee; }
    public Integer getDueDay() { return dueDay; }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }
    public String getCurrentPendingMonth() { return currentPendingMonth; }
    public void setCurrentPendingMonth(String currentPendingMonth) { this.currentPendingMonth = currentPendingMonth; }
    public boolean isCurrentMonthPaid() { return isCurrentMonthPaid; }
    public void setCurrentMonthPaid(boolean currentMonthPaid) { this.isCurrentMonthPaid = currentMonthPaid; }
    public String getPaymentTimestamp() { return paymentTimestamp; }
    public void setPaymentTimestamp(String paymentTimestamp) { this.paymentTimestamp = paymentTimestamp; }
}