package com.hospital.token.model;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", unique = true)
    private String doctorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorStatus status = DoctorStatus.AVAILABLE;

    @Column(nullable = false)
    private Integer dailyTokenLimit = 30;

    @Column(nullable = false)
    private Integer tokensIssuedToday = 0;

    @Column(nullable = false)
    private Integer currentWaitingCount = 0;

    private String roomNumber;

    private String consultationStartTime;

    private String consultationEndTime;

    private boolean active = true;

    public Doctor() {
    }

    public Doctor(String name,
                  String specialization,
                  Department department,
                  Integer dailyTokenLimit,
                  String roomNumber) {

        this.name = name;
        this.specialization = specialization;
        this.department = department;
        this.dailyTokenLimit = dailyTokenLimit;
        this.roomNumber = roomNumber;
        this.status = DoctorStatus.AVAILABLE;
        this.tokensIssuedToday = 0;
        this.currentWaitingCount = 0;
        this.active = true;
    }

    public boolean canAcceptPatient() {

        return active
                && status == DoctorStatus.AVAILABLE
                && tokensIssuedToday < dailyTokenLimit;
    }

    public int getRemainingTokens() {
        return dailyTokenLimit - tokensIssuedToday;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public DoctorStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorStatus status) {
        this.status = status;
    }

    public Integer getDailyTokenLimit() {
        return dailyTokenLimit;
    }

    public void setDailyTokenLimit(Integer dailyTokenLimit) {
        this.dailyTokenLimit = dailyTokenLimit;
    }

    public Integer getTokensIssuedToday() {
        return tokensIssuedToday;
    }

    public void setTokensIssuedToday(Integer tokensIssuedToday) {
        this.tokensIssuedToday = tokensIssuedToday;
    }

    public Integer getCurrentWaitingCount() {
        return currentWaitingCount;
    }

    public void setCurrentWaitingCount(Integer currentWaitingCount) {
        this.currentWaitingCount = currentWaitingCount;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getConsultationStartTime() {
        return consultationStartTime;
    }

    public void setConsultationStartTime(String consultationStartTime) {
        this.consultationStartTime = consultationStartTime;
    }

    public String getConsultationEndTime() {
        return consultationEndTime;
    }

    public void setConsultationEndTime(String consultationEndTime) {
        this.consultationEndTime = consultationEndTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}