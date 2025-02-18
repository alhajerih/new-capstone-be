package com.example.Shares.auth.entity;


import com.example.Shares.auth.utils.Roles;
import com.example.Shares.hub.entity.HubEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String civilId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String phoneNumber;
    private String otp;
   private String pictureUrl;
    private LocalDateTime otpExpiry;
    private Boolean smartPay = false;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankCardEntity> bankCards = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hub_id", nullable = false)
    @JsonManagedReference
    private HubEntity hub;


    @Enumerated(EnumType.STRING)
    private Roles role;

    // Getters and Setters

    public Boolean getSmartPay() {
        return smartPay;
    }

    public void setSmartPay(Boolean smartPay) {
        this.smartPay = smartPay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCivilId() {
        return civilId;
    }

    public void setCivilId(String civilId) {
        this.civilId = civilId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public List<BankCardEntity> getBankCards() {
        return bankCards;
    }

    public void setBankCards(List<BankCardEntity> bankCards) {
        this.bankCards = bankCards;
    }

    public HubEntity getHub() {
        return hub;
    }

    public void setHub(HubEntity hub) {
        this.hub = hub;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
