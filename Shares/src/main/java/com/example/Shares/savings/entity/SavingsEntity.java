package com.example.Shares.savings.entity;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "savings")
public class SavingsEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double totalAmount;
    private Integer months;
    private Double monthlyPayment;
    private Integer paymentsMade = 0;
    private Boolean active = true;

    // The card we deduct money from (checking)
    @ManyToOne
    @JoinColumn(name = "from_card_id", nullable = false)
    private BankCardEntity fromCard;

    // The card we add money to (savings)
    @ManyToOne
    @JoinColumn(name = "to_card_id", nullable = false)
    private BankCardEntity toCard;

    // The user who owns this savings plan
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity owner;

    // Constructors
    public SavingsEntity() { }

    // Getters/Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getMonths() {
        return months;
    }
    public void setMonths(Integer months) {
        this.months = months;
    }

    public Double getMonthlyPayment() {
        return monthlyPayment;
    }
    public void setMonthlyPayment(Double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public Integer getPaymentsMade() {
        return paymentsMade;
    }
    public void setPaymentsMade(Integer paymentsMade) {
        this.paymentsMade = paymentsMade;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public BankCardEntity getFromCard() {
        return fromCard;
    }
    public void setFromCard(BankCardEntity fromCard) {
        this.fromCard = fromCard;
    }

    public BankCardEntity getToCard() {
        return toCard;
    }
    public void setToCard(BankCardEntity toCard) {
        this.toCard = toCard;
    }

    public UserEntity getOwner() {
        return owner;
    }
    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
}
