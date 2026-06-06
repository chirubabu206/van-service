package com.vanservice.van_servicce.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "students")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "monthly_fee", nullable = false)
    private double monthlyFee;

    @Column(name = "current_pending_fee")
    private double currentPendingFee;

    @Column(name = "pending_months")
    private String pendingMonths;

    @Column(name = "current_payment_status")
    private boolean currentPaymentStatus;

    @Column(name = "payment_timestamp")
    private String paymentTimestamp;

    // 🔗 RELATIONAL CONNECTION: Maps each student row to their respective User account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Set to true initially to prevent legacy crash mapping
    private User user;

    // ==================== CONSTRUCTORS ====================
    public Student() {
    }

    public Student(String studentName, double monthlyFee) {
        this.studentName = studentName;
        this.monthlyFee = monthlyFee;
    }

    // ==================== GETTERS AND SETTERS ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public double getCurrentPendingFee() {
        return currentPendingFee;
    }

    public void setCurrentPendingFee(double currentPendingFee) {
        this.currentPendingFee = currentPendingFee;
    }

    public String getPendingMonths() {
        return pendingMonths;
    }

    public void setPendingMonths(String pendingMonths) {
        this.pendingMonths = pendingMonths;
    }

    public boolean isCurrentPaymentStatus() {
        return currentPaymentStatus;
    }

    public void setCurrentPaymentStatus(boolean currentPaymentStatus) {
        this.currentPaymentStatus = currentPaymentStatus;
    }

    public String getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public void setPaymentTimestamp(String paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}