package com.example.Shares.transactions.entity;

import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.wallet.entity.WalletEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class TransactionsEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionName;  // e.g., "Purchase at Store XYZ"
    private Double amount;
    private Double longitude;
    private Double latitude;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionTime;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    @JsonBackReference
    private WalletEntity walletUsed;

    @ManyToOne
    @JoinColumn(name = "hub_id")
    @JsonBackReference
    private HubEntity hub;

    @PrePersist
    protected void onCreate() {
        this.transactionTime = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public WalletEntity getWalletUsed() {
        return walletUsed;
    }

    public void setWalletUsed(WalletEntity walletUsed) {
        this.walletUsed = walletUsed;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public HubEntity getHub() {
        return hub;
    }

    public void setHub(HubEntity hub) {
        this.hub = hub;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
