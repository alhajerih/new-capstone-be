package com.example.Shares.auth.bo;

public class BankCardRequest {
    private String bankName;
    private Double balance;
    private String cardType;
    private Boolean salaryAccount;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Boolean getSalaryAccount() {
        return salaryAccount;
    }

    public void setSalaryAccount(Boolean salaryAccount) {
        this.salaryAccount = salaryAccount;
    }
}
