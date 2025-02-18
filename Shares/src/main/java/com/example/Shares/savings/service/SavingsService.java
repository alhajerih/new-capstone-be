package com.example.Shares.savings.service;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.repository.BankCardRepository;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.savings.bo.CreateSavingsRequest;
import com.example.Shares.savings.entity.SavingsEntity;
import com.example.Shares.savings.repository.SavingsRepository;
import com.example.Shares.wallet.entity.WalletEntity;
import com.example.Shares.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class SavingsService {

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private WalletRepository walletRepository;


    @Transactional
    public SavingsEntity createSavingsGoal(UserEntity currentUser, CreateSavingsRequest request) {
        BankCardEntity fromCard = bankCardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new IllegalArgumentException("FromCard not found with ID " + request.getFromCardId()));

        BankCardEntity toCard = bankCardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new IllegalArgumentException("ToCard not found with ID " + request.getToCardId()));

        SavingsEntity savings = new SavingsEntity();
        savings.setName(request.getName());
        savings.setTotalAmount(request.getTotalAmount());
        savings.setMonths(request.getMonths());
        savings.setMonthlyPayment(request.getTotalAmount() / request.getMonths());
        savings.setFromCard(fromCard);
        savings.setToCard(toCard);
        savings.setOwner(currentUser);
        savings.setPaymentsMade(0);
        savings.setActive(true);

        return savingsRepository.save(savings);
    }


    @Transactional
    public SavingsEntity processMonthlyPayment(Long savingsId) {
        SavingsEntity savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new IllegalArgumentException("Savings not found with ID " + savingsId));

        if (!savings.getActive()) {
            throw new IllegalStateException("Savings is already inactive or completed.");
        }

        BankCardEntity fromCard = savings.getFromCard();
        Double monthly = savings.getMonthlyPayment();
        if (fromCard.getCardBalance() < monthly) {
            throw new IllegalStateException("Insufficient balance on fromCard (ID " + fromCard.getId() + ")");
        }
        fromCard.setCardBalance(fromCard.getCardBalance() - monthly);
        bankCardRepository.save(fromCard);

        BankCardEntity toCard = savings.getToCard();
        toCard.setCardBalance(toCard.getCardBalance() + monthly);
        bankCardRepository.save(toCard);

        savings.setPaymentsMade(savings.getPaymentsMade() + 1);

        if (savings.getPaymentsMade().equals(savings.getMonths())) {
            savings.setActive(false);
            createWalletFromSavings(savings);
        }

        return savingsRepository.save(savings);
    }


    private void createWalletFromSavings(SavingsEntity savings) {
        UserEntity user = savings.getOwner();
        HubEntity hub = user.getHub();
        if (hub == null) {
            throw new IllegalStateException("User has no hub to attach the new wallet to.");
        }

        WalletEntity wallet = new WalletEntity();
        wallet.setName("Wallet from Savings: " + savings.getName());
        wallet.setBalance(0.0);
        wallet.setLocked(false);
        wallet.setSelected(false);
        wallet.setHub(hub);

        wallet.getLinkedCards().add(savings.getToCard());

        walletRepository.save(wallet);


        hub.getWallets().add(wallet);


        hub.updateBalances();
    }
}
