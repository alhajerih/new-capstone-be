package com.example.Shares.hub.bo;

public class HubCardPaymentRequest {
    private String hubCardNumber;
    private Double amount;
    private String transactionName;
    private String transactionId;
    private Double longitude;
    private Double latitude;

    // Getters and Setters
    public String getHubCardNumber() {
        return hubCardNumber;
    }

    public void setHubCardNumber(String hubCardNumber) {
        this.hubCardNumber = hubCardNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    @Override
    public String toString() {
        return "HubCardPaymentRequest{" +
                "hubCardNumber='" + hubCardNumber + '\'' +
                ", amount=" + amount +
                ", transactionName='" + transactionName + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
