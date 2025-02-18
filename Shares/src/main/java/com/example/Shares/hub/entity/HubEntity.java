package com.example.Shares.hub.entity;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.wallet.entity.WalletEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hub")
public class HubEntity implements Serializable {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isActive;
    private Double savingsBalance = 0.0;
    private Double checkingsBalance = 0.0;

    private String hubCardNumber;
    private String expDate;
    private String cvv;
    private Integer cardTheme = 1; // Default theme is 1

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<WalletEntity> wallets = new ArrayList<>();

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TransactionsEntity> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankCardEntity> linkedCards = new ArrayList<>();

    // ------------------------ Lifecycle Callbacks ------------------------
    @PrePersist
    @PreUpdate
    public void generateHubDetails() {
        if (hubCardNumber == null || hubCardNumber.isEmpty()) {
            hubCardNumber = generateRandomNumber(16); // Example
        }
        if (cvv == null || cvv.isEmpty()) {
            cvv = generateRandomNumber(3);
        }
        if (expDate == null || expDate.isEmpty()) {
            expDate = generateExpirationDate();
        }
    }

    private String generateRandomNumber(int length) {
        StringBuilder number = new StringBuilder("222");
        for (int i = 3; i < length; i++) {
            number.append(RANDOM.nextInt(10));
        }
        return number.toString();
    }

    private String generateExpirationDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate expirationDate = currentDate.plusYears(5);
        return expirationDate.format(DATE_FORMATTER);
    }

    /**
     * Recompute the aggregator fields from all linked cards.
     */
    public void updateBalances() {
        this.savingsBalance = linkedCards.stream()
                .filter(card -> "savings".equalsIgnoreCase(card.getCardType()))
                .mapToDouble(BankCardEntity::getCardBalance)
                .sum();

        this.checkingsBalance = linkedCards.stream()
                .filter(card -> "checking".equalsIgnoreCase(card.getCardType()))
                .mapToDouble(BankCardEntity::getCardBalance)
                .sum();
    }

    // Convenience methods
    public void addLinkedCard(BankCardEntity card) {
        linkedCards.add(card);
        card.setHub(this);
        updateBalances();
    }

    public void removeLinkedCard(BankCardEntity card) {
        linkedCards.remove(card);
        card.setHub(null);
        updateBalances();
    }

    public void addTransaction(TransactionsEntity transaction) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(transaction);
    }

    // ------------------------ Getters / Setters ------------------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Double getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(Double savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public Double getCheckingsBalance() {
        return checkingsBalance;
    }

    public void setCheckingsBalance(Double checkingsBalance) {
        this.checkingsBalance = checkingsBalance;
    }

    public String getHubCardNumber() {
        return hubCardNumber;
    }

    public void setHubCardNumber(String hubCardNumber) {
        this.hubCardNumber = hubCardNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Integer getCardTheme() {
        return cardTheme;
    }

    public void setCardTheme(Integer cardTheme) {
        if (cardTheme >= 1 && cardTheme <= 6) {
            this.cardTheme = cardTheme;
        }
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<WalletEntity> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletEntity> wallets) {
        this.wallets = wallets;
    }

    public List<BankCardEntity> getLinkedCards() {
        return linkedCards;
    }

    public void setLinkedCards(List<BankCardEntity> linkedCards) {
        this.linkedCards = linkedCards;
    }

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionsEntity> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "HubEntity{" +
                "id=" + id +
                ", isActive=" + isActive +
                ", savingsBalance=" + savingsBalance +
                ", checkingsBalance=" + checkingsBalance +
                ", hubCardNumber='" + hubCardNumber + '\'' +
                ", expDate='" + expDate + '\'' +
                ", cvv='" + cvv + '\'' +
                ", user=" + user +
                ", wallets=" + wallets +
                ", transactions=" + transactions +
                ", linkedCards=" + linkedCards +
                '}';
    }
}
