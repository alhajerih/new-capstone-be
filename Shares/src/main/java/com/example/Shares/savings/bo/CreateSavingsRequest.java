package com.example.Shares.savings.bo;

public class CreateSavingsRequest {
    private String name;
    private Double totalAmount;
    private Integer months;
    private Long fromCardId;
    private Long toCardId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Integer getMonths() { return months; }
    public void setMonths(Integer months) { this.months = months; }

    public Long getFromCardId() { return fromCardId; }
    public void setFromCardId(Long fromCardId) { this.fromCardId = fromCardId; }

    public Long getToCardId() { return toCardId; }
    public void setToCardId(Long toCardId) { this.toCardId = toCardId; }
}
