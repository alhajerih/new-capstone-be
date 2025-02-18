package com.example.Shares.transactions.repository;

import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.wallet.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TransactionsRepository extends JpaRepository<TransactionsEntity, Long> {
    Page <TransactionsEntity> findByWalletUsed(WalletEntity walletUsed , Pageable pageable);
    Page <TransactionsEntity> findByHub(HubEntity hub , Pageable pageable);
}




