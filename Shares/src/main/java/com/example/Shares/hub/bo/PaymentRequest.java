package com.example.Shares.hub.bo;

public class PaymentRequest {
    private double amount;
    private String transactionName;
    private Double longitude;
    private Double latitude;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(double amount, String transactionName, Double longitude, Double latitude) {
        this.amount = amount;
        this.transactionName = transactionName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters and Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
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
