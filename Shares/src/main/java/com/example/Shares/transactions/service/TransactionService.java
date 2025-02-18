package com.example.Shares.transactions.service;

import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.transactions.repository.TransactionsRepository;
import com.example.Shares.wallet.entity.WalletEntity;
import com.example.Shares.wallet.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

    private final TransactionsRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final HubRepository hubRepository;
    public TransactionService(TransactionsRepository transactionRepository, WalletRepository walletRepository, HubRepository hubRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.hubRepository = hubRepository;
    }

    public Page<TransactionsEntity> getTransactionsByWalletId(Long walletId, Pageable pageable) {
        WalletEntity wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        return transactionRepository.findByWalletUsed(wallet, pageable);
    }

    public  Page<TransactionsEntity> getTransactionByHubId(Long hubId , Pageable pageable){
        // fetch hub entity by id
        HubEntity hub = hubRepository.findById(hubId).orElseThrow(()-> new RuntimeException("Hub not found"));

        return transactionRepository.findByHub(hub,pageable);

    }
}
