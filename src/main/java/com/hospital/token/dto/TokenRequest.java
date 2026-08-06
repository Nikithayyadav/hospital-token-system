package com.hospital.token.dto;

public class TokenRequest {

    // Hospital-generated Patient ID, e.g. P00009
    private String patientId;

    // Internal doctor database ID
    private Long doctorId;

    // NORMAL or EMERGENCY
    private String type;

    private String reasonForVisit;

    // Required only for EMERGENCY
    private String priority;


    public TokenRequest() {
    }


    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }


    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }


    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}