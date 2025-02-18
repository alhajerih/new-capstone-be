package com.example.Shares.wallet.service;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.transactions.repository.TransactionsRepository;
import com.example.Shares.wallet.bo.CreateWalletRequest;
import com.example.Shares.wallet.bo.UpdateWalletRequest;
import com.example.Shares.wallet.entity.WalletEntity;
import com.example.Shares.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;


    @Transactional
    public void createWallet(CreateWalletRequest request, UserEntity user) {
        WalletEntity wallet = new WalletEntity();
        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance());
        wallet.setPatternId(request.getPatternID());
        wallet.setColorId(request.getColorId());
        wallet.setCategory(request.getCategory());
        wallet.setAllocation(request.getBalance());
        wallet.setLocked(false);
        wallet.setHub(user.getHub());


        String cardNumber = request.getCardNumber();

        BankCardEntity linkedCard = user.getHub().getLinkedCards().stream()
                .filter(card -> card.getCardNumber() != null && card.getCardNumber().equals(cardNumber))
                .findFirst()
                .orElse(null);

        if (linkedCard != null) {
            wallet.getLinkedCards().add(linkedCard);
        } else {
            throw new IllegalArgumentException("The provided card is not linked to the hub.");
        }

        walletRepository.save(wallet);
    }

    @Transactional
    public void updateWallet(UserEntity user, UpdateWalletRequest wallet) {
        WalletEntity existingWallet = user.getHub().getWallets().stream()
                .filter(w -> w.getId().equals(wallet.getWalletId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Update fields dynamically if not null
        Optional.ofNullable(wallet.getName()).ifPresent(existingWallet::setName);
        Optional.ofNullable(wallet.getPatternId()).ifPresent(existingWallet::setPatternId);
        Optional.ofNullable(wallet.getColorId()).ifPresent(existingWallet::setColorId);
        Optional.ofNullable(wallet.getCategory()).ifPresent(existingWallet::setCategory);
        Optional.ofNullable(wallet.getAllocation()).ifPresent(existingWallet::setAllocation);
        Optional.ofNullable(wallet.getBalance()).ifPresent(existingWallet::setBalance);

        // Save the updated wallet
        walletRepository.save(existingWallet);
    }


    @Transactional
    public void deleteWallet(UserEntity user, Long walletId) {
        WalletEntity existingWallet = user.getHub().getWallets().stream()
                .filter(w -> w.getId().equals(walletId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Remove from the user's hub wallet list before deletion
        user.getHub().getWallets().remove(existingWallet);

        System.out.println("Deleting wallet: " + existingWallet.getName());
        walletRepository.delete(existingWallet);
    }

    @Transactional
    public void selectWallet(UserEntity user, Long walletId) {
        HubEntity hub = user.getHub();

        // Deselect any previously selected wallet
        hub.getWallets().forEach(wallet -> wallet.setSelected(false));

        // Find and select the new wallet
        WalletEntity selectedWallet = hub.getWallets().stream()
                .filter(wallet -> wallet.getId().equals(walletId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        selectedWallet.setSelected(true);
        walletRepository.saveAll(hub.getWallets());
    }





    @Transactional
    public void deselectAllWallets(UserEntity user) {
        // Retrieve the HubEntity from the user
        HubEntity hub = user.getHub();
        if (hub == null) {
            throw new IllegalStateException("User does not have an associated Hub.");
        }

        // Deselect any previously selected wallet
        hub.getWallets().forEach(wallet -> wallet.setSelected(false));

        // Persist changes
        walletRepository.saveAll(hub.getWallets());
        hubRepository.save(hub);
    }

    public List<WalletEntity> getAllWalletsForUser(UserEntity user) {
        HubEntity hub = user.getHub();
        if (hub == null) {
            return Collections.emptyList(); // No hub means no wallets
        }
        return hub.getWallets();
    }

    public void toggleWalletActiveStatus(UserEntity user, Long walletId) {
        WalletEntity wallet = user.getHub().getWallets().stream()
                .filter(w -> w.getId().equals(walletId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        wallet.setLocked(!wallet.getLocked());

        walletRepository.save(wallet);
    }


}

