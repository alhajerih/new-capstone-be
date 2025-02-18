package com.example.Shares.wallet.bo;

public class CreateWalletRequest {


    private String name;
    private Double balance;
    private String cardNumber;  // New field for card number
    private Long patternID;
    private Long colorId;
    private String category;

    public CreateWalletRequest() {
        //Default constructor
    }




    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getPatternID() {
        return patternID;
    }

    public void setPatternID(Long patternID) {
        this.patternID = patternID;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }
}




