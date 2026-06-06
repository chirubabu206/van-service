package com.vanservice.van_servicce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double monthlyFee;
    private double currentPendingFee;
    private int dueDay;
    private boolean isCurrentPaymentStatus;
    private String paymentTimestamp;
    private String pendingMonths = "June"; // Default starting month when a student is added

    // 🔀 NEW FIELDS: Added to capture registration details from your HTML form!
    private String pickupLocation;
    private String phone;

    // ===================================================================
    // GETTERS AND SETTERS
    // ===================================================================

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public int getDueDay() {
        return dueDay;
    }
    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public boolean isCurrentPaymentStatus() {
        return isCurrentPaymentStatus;
    }
    public void setCurrentPaymentStatus(boolean isCurrentPaymentStatus) {
        this.isCurrentPaymentStatus = isCurrentPaymentStatus;
    }

    public String getPaymentTimestamp() {
        return paymentTimestamp;
    }
    public void setPaymentTimestamp(String paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public String getPendingMonths() {
        return pendingMonths;
    }
    public void setPendingMonths(String pendingMonths) {
        this.pendingMonths = pendingMonths;
    }

    // 🔄 NEW GETTERS & SETTERS: Links your fields directly to your frontend variables
    public String getPickupLocation() {
        return pickupLocation;
    }
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}