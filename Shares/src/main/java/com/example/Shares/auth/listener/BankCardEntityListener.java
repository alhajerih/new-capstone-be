package com.example.Shares.auth.listener;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.hub.entity.HubEntity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class BankCardEntityListener {

    @PrePersist
    @PreUpdate
    public void onPrePersistOrUpdate(BankCardEntity card) {
        HubEntity hub = card.getHub();
        if (hub != null) {
            // Recompute aggregator fields (savingsBalance, checkingsBalance).
            hub.updateBalances();
            // Since hub is in the same persistence context,
            // JPA will pick up its changes and persist them on commit/flush.
        }
    }
}
