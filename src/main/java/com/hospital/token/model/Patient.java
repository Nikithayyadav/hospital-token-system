package com.hospital.token.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient implements Comparable<Patient> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", unique = true)
    private String patientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String phone;

    private String address;

    private String bloodGroup;

    private boolean active = true;

    private LocalDateTime registeredAt;

    public Patient() {
    }

    public Patient(String name, Integer age, String gender,
                   String phone, String address, String bloodGroup) {

        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.active = true;
        this.registeredAt = LocalDateTime.now();
    }

    @PrePersist
    public void beforeSave() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    @Override
    public int compareTo(Patient other) {

        int nameComparison =
                this.name.compareToIgnoreCase(other.name);

        if (nameComparison != 0) {
            return nameComparison;
        }

        if (this.id == null || other.id == null) {
            return 0;
        }

        return this.id.compareTo(other.id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}