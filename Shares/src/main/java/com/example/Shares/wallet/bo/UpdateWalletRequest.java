package com.example.Shares.wallet.bo;


public class UpdateWalletRequest {

    private Long walletId;
    private String name;
    private Double balance;
    private Long patternId;
    private String category;
    private Double allocation;
    private Long colorId;

    public UpdateWalletRequest() {
        //Default constructor
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }


    public Long getPatternId() {
        return patternId;
    }

    public void setPatternId(Long patternId) {
        this.patternId = patternId;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Double getAllocation() {
        return allocation;
    }

    public void setAllocation(Double allocation) {
        this.allocation = allocation;
    }
}
