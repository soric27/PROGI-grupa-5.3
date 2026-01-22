package com.autoservis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "servis_info")
public class ServisInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "working_hours")
    private String workingHours;

    protected ServisInfo() {}

    public ServisInfo(String contactEmail, String contactPhone, String aboutText, Double latitude, Double longitude, String workingHours) {
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.aboutText = aboutText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.workingHours = workingHours;
    }

    public Long getId() { return id; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public String getAboutText() { return aboutText; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getWorkingHours() { return workingHours; }

    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setAboutText(String aboutText) { this.aboutText = aboutText; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
}
