package com.example.Shares.auth.service;

import com.example.Shares.auth.bo.AddCardToHubRequest;
import com.example.Shares.auth.bo.BankCardRequest;
import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.repository.BankCardRepository;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class BankCardService {

    @Autowired
    private BankCardRepository cardBankRepository;

    @Autowired
    private HubRepository hubRepository;

    public void createCard(BankCardRequest request, UserEntity user) {
        BankCardEntity bankCard = new BankCardEntity();
        bankCard.setBankName(request.getBankName());
        bankCard.setCardBalance(request.getBalance());
        bankCard.setCardType(request.getCardType());
        bankCard.setUser(user);
        bankCard.setSalaryAccount(request.getSalaryAccount());

        if (user.getBankCards() == null) {
            user.setBankCards(new ArrayList<>());
        }
        user.getBankCards().add(bankCard);

        cardBankRepository.save(bankCard);

        // Update the hub balances after adding a new card
        user.getHub().updateBalances();
        hubRepository.save(user.getHub());
    }

    public void addCardToHub(AddCardToHubRequest request, UserEntity user) {
        Optional<BankCardEntity> bankCardOptional = cardBankRepository.findByCardNumber(request.getCardNumber());

        if (bankCardOptional.isPresent()) {
            BankCardEntity bankCard = bankCardOptional.get();
            HubEntity hub = user.getHub();  // Get hub directly from the UserEntity

            if (hub == null) {
                throw new IllegalArgumentException("User does not have an associated hub.");
            }

            if (hub.getLinkedCards() == null) {
                hub.setLinkedCards(new ArrayList<>());
            }

            // Link the card to the hub and update balance
            bankCard.setSelected(true);
            bankCard.setHub(hub);
            hub.getLinkedCards().add(bankCard);

            // Update the hub's total balances
            hub.updateBalances();

            // Save the updated entities
            cardBankRepository.save(bankCard);
            hubRepository.save(hub);
        } else {
            throw new IllegalArgumentException("Invalid card number provided.");
        }
    }

    public void removeCardFromHub(AddCardToHubRequest request, UserEntity user) {
        Optional<BankCardEntity> bankCardOptional = cardBankRepository.findByCardNumber(request.getCardNumber());

        if (bankCardOptional.isPresent()) {
            BankCardEntity bankCard = bankCardOptional.get();
            HubEntity hub = user.getHub();  // Get hub directly from the UserEntity

            if (hub == null) {
                throw new IllegalArgumentException("User does not have an associated hub.");
            }

            if (hub.getLinkedCards() != null && hub.getLinkedCards().contains(bankCard)) {
                // Remove the card from the hub's linked list
                hub.getLinkedCards().remove(bankCard);
                bankCard.setHub(null);
                bankCard.setSelected(false);

                // Update the hub's total balances after removal
                hub.updateBalances();

                // Save the updated entities
                cardBankRepository.save(bankCard);
                hubRepository.save(hub);
            } else {
                throw new IllegalArgumentException("The provided card is not linked to the hub.");
            }
        } else {
            throw new IllegalArgumentException("Invalid card number provided.");
        }
    }
}
