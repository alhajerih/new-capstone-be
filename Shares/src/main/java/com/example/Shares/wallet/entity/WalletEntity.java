package com.example.Shares.wallet.entity;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
public class WalletEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double balance;
    private Boolean locked;
    private Boolean selected = false;
    private Long colorId;
    private Long patternId;
    private String category;
    private Double allocation;
    @ManyToOne
    @JoinColumn(name = "hub_id", referencedColumnName = "id")
    @JsonBackReference
    private HubEntity hub;

    @ManyToMany
    @JsonManagedReference
    @JoinTable(
            name = "wallets_cards",
            joinColumns = @JoinColumn(name = "wallet_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id")
    )
    private List<BankCardEntity> linkedCards = new ArrayList<>();


    @OneToMany(mappedBy = "walletUsed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TransactionsEntity> transactions;

    // Getters and Setters

    public Double getAllocation() {
        return allocation;
    }

    public void setAllocation(Double allocation) {
        this.allocation = allocation;
    }



    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean active) {
        this.locked = active;
    }

    public HubEntity getHub() {
        return hub;
    }

    public void setHub(HubEntity hub) {
        this.hub = hub;
    }

    public List<BankCardEntity> getLinkedCards() {
        return linkedCards;
    }

    public void setLinkedCards(List<BankCardEntity> linkedCards) {
        this.linkedCards = linkedCards;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionsEntity> transactions) {
        this.transactions = transactions;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public Long getPatternId() {
        return patternId;
    }

    public void setPatternId(Long patternId) {
        this.patternId = patternId;
    }
}
