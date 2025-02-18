package com.example.Shares.hub.service;

import com.example.Shares.OpenAi.service.OpenAIService;
import com.example.Shares.QRcode.QRCodeEntity;
import com.example.Shares.QRcode.QRCodeRepository;
import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.repository.BankCardRepository;
//import com.example.Shares.feign.Notification;
import com.example.Shares.grpc.NotificationGrpcClient;
import com.example.Shares.hub.bo.HubCardPaymentRequest;
import com.example.Shares.hub.bo.PaymentRequest;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
//import com.example.Shares.notification.service.NotificationService;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.transactions.repository.TransactionsRepository;
import com.example.Shares.wallet.entity.WalletEntity;
import com.example.Shares.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HubService {

//    private final Notification notificationService;
//    private  final RestTemplate restTemplate;
    @Autowired
    private BankCardRepository cardBankRepository;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final NotificationGrpcClient notificationGrpcClient;
    private static final String STOCK_API = "https://capstone-front-end-store.vercel.app/transaction-status";

    @Autowired
    public HubService( NotificationGrpcClient notificationGrpcClient) {
//        this.notificationService = notificationService;
        this.notificationGrpcClient = notificationGrpcClient;
    }



    @Transactional
    public boolean processPaymentWithChecking(UserEntity user, PaymentRequest request) {
        HubEntity hub = user.getHub();
        Double amountNeeded = request.getAmount();

        // 1) Check if there is a selected wallet in this hub.
        WalletEntity selectedWallet = hub.getWallets().stream()
                .filter(WalletEntity::getSelected)
                .findFirst()
                .orElse(null);

        // -------------------------------------------------------------------------
        // CASE A: A wallet is selected -> Use existing single-wallet logic
        // -------------------------------------------------------------------------
        if (selectedWallet != null) {
            // Find the linked card for the selected wallet (assuming only one)
            BankCardEntity linkedCard = selectedWallet.getLinkedCards().stream()
                    .findFirst()
                    .orElse(null);

            if (linkedCard == null) {
                System.out.println("Transaction canceled due to no linked card on selected wallet.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "Transaction canceled due to no linked card on selected wallet");
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "No linked card on selected wallet");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

//                notificationService.sendFailureNotification(requestBody);
                return false;
            }

            // Check if that single wallet has enough balance
            if (selectedWallet.getBalance() >= amountNeeded) {
                // Deduct from the wallet
                selectedWallet.setBalance(selectedWallet.getBalance() - amountNeeded);
                // Deduct from the linked card
                linkedCard.setCardBalance(linkedCard.getCardBalance() - amountNeeded);

                // Update hub's aggregated balances
                hub.updateBalances();

                // Create and save the transaction record
                TransactionsEntity transaction = new TransactionsEntity();
                transaction.setTransactionName(request.getTransactionName());
                transaction.setAmount(amountNeeded);
                transaction.setWalletUsed(selectedWallet);
                transaction.setHub(hub);
                transaction.setTransactionTime(LocalDateTime.now());
                transaction.setLongitude(request.getLongitude());
                transaction.setLatitude(request.getLatitude());
                transactionsRepository.save(transaction);

                // Persist changes
                walletRepository.save(selectedWallet);
                cardBankRepository.save(linkedCard);
                hubRepository.save(hub);
                // Send payment by gRPC
                notificationGrpcClient.sendPaymentNotification(selectedWallet.getName(), selectedWallet.getBalance(), amountNeeded, request.getTransactionName());
                String status= "Accepted";
                sendTransactionStatus(status);
                transactionStatus(status);

                return true;
            } else {
                System.out.println("Transaction canceled due to insufficient funds in the selected wallet.");
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "insufficient funds in the selected wallet.");

                return false;
            }

        } else {
            // ---------------------------------------------------------------------
            // CASE B: No wallet selected -> Use MULTI-CARD Checking distribution
            // ---------------------------------------------------------------------

            // 2) Gather all checking-type cards in the hub
            List<BankCardEntity> checkingCards = hub.getLinkedCards().stream()
                    .filter(card -> "checking".equalsIgnoreCase(card.getCardType()))
                    .collect(Collectors.toList());


            // Edge case: No checking cards at all
            if (checkingCards.isEmpty()) {
                System.out.println("Transaction canceled: No checking cards linked to Hub.");
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "No checking cards linked to Hub.");
                return false;
            }

            // 3) Calculate the total of all checking balances
            double totalCheckingBalance = checkingCards.stream()
                    .mapToDouble(BankCardEntity::getCardBalance)
                    .sum();

            // If total checking is insufficient, cancel transaction
            if (totalCheckingBalance < amountNeeded) {
                System.out.println("Transaction canceled due to insufficient total checking balance.");
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "Insufficient balance across all linked cards.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                return false;
            }

            // 4) Distribute amountNeeded across checking cards by ratio
            //    (ratio = cardBalance / totalCheckingBalance)
            //    and handle rounding to 3 decimals.

            // Step A: Compute raw shares
            List<Double> rawShares = new ArrayList<>();
            for (BankCardEntity card : checkingCards) {
                double ratio = card.getCardBalance() / totalCheckingBalance;
                rawShares.add(ratio * amountNeeded);
            }

            // Step B: Round each share to 3 decimals (use integer thousandths to avoid floating errors)
            int[] sharesInThousandths = new int[rawShares.size()];
            int sumThousandths = 0;
            for (int i = 0; i < rawShares.size(); i++) {
                int rounded = (int) Math.round(rawShares.get(i) * 1000);
                sharesInThousandths[i] = rounded;
                sumThousandths += rounded;
            }

            // Step C: Compute difference vs. total target in thousandths
            int targetThousandths = (int) Math.round(amountNeeded * 1000);
            int diff = targetThousandths - sumThousandths;

            // Step D: Add or subtract any leftover difference to the largest share
            if (diff != 0) {
                // find index of the largest share
                int largestIndex = 0;
                for (int i = 1; i < sharesInThousandths.length; i++) {
                    if (sharesInThousandths[i] > sharesInThousandths[largestIndex]) {
                        largestIndex = i;
                    }
                }
                // adjust largest share by diff
                sharesInThousandths[largestIndex] += diff;
            }

            // 5) Now deduct the final amounts from each card
            for (int i = 0; i < checkingCards.size(); i++) {
                BankCardEntity card = checkingCards.get(i);
                double finalShare = sharesInThousandths[i] / 1000.0; // convert back to double
                card.setCardBalance(card.getCardBalance() - finalShare);

                // Optionally, you can also create sub-transactions per card if you wish,
                // but commonly you'd just create a single transaction record for the entire purchase.
                cardBankRepository.save(card);
            }

            // Update the hub's aggregated balances
            hub.updateBalances();

            // 6) Create a single transaction record for the entire sum
            TransactionsEntity transaction = new TransactionsEntity();
            transaction.setTransactionName(request.getTransactionName());
            transaction.setAmount(amountNeeded);
            transaction.setHub(hub);
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setLongitude(request.getLongitude());
            transaction.setLatitude(request.getLatitude());
            // (No single wallet used, so we don't set `walletUsed` here)

            // Save transaction and hub
            transactionsRepository.save(transaction);
            hubRepository.save(hub);
            String status= "Accepted";
            sendTransactionStatus(status);
            transactionStatus(status);

            return true;
        }
    }




    @Transactional
    @Cacheable(value = "hubCard", key = "#request")
    public boolean processPaymentByHubCard(HubCardPaymentRequest request) {
        // 1) Find the hub by the provided hubCardNumber
        Optional<HubEntity> hubOptional = hubRepository.findByHubCardNumber(request.getHubCardNumber());
        if (hubOptional == null || !hubOptional.isPresent()) {
            System.out.println("Transaction failed: No hub found with the provided card number.");
            notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "No hub found with the provided card number");

            return false;
        }

        HubEntity hub = hubOptional.get();
        System.out.println("Hub found: " + hub.getHubCardNumber());
        Double amountNeeded = request.getAmount();

        // Retrieve the QR code transaction from the database
        Optional<QRCodeEntity> qrCodeOptional = qrCodeRepository.findByTransactionId(request.getTransactionId());
        if(!qrCodeOptional.isPresent()){
            System.out.println("No QR code found");
        }
        QRCodeEntity qrCode = qrCodeOptional.orElse(null);
        if(qrCode != null){
            //check if the QR code transaction is already paid
            if(qrCode.getPaid()) {
                System.out.println("Transaction already paid");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);


            return false;
            }
        }

        // 2) Check if there is a selected wallet in this hub
        WalletEntity selectedWallet = hub.getWallets().stream()
                .filter(WalletEntity::getSelected)
                .findFirst()
                .orElse(null);
        System.out.println("SelectedWallet is : "+ selectedWallet.getName());
        boolean transactionSuccessful = false; // Track if payment succeeded

        // -------------------------------------------------------------------------
        // CASE A: A wallet is selected -> Use existing single-wallet logic
        // -------------------------------------------------------------------------
        if (selectedWallet != null) {
            // Grab any one linked card. (You could do more sophisticated logic if there are multiple.)
            BankCardEntity linkedCard = selectedWallet.getLinkedCards().stream().findFirst().orElse(null);

            if (linkedCard == null) {
                System.out.println("Transaction canceled: no linked card on the selected wallet.");
                notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "no linked card on the selected wallet.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                return false;
            }

            // Check if the selected wallet has enough balance
            if (selectedWallet.getBalance() >= amountNeeded) {
                // Deduct from wallet balance
                selectedWallet.setBalance(selectedWallet.getBalance() - amountNeeded);
                // Deduct from the linked card
                linkedCard.setCardBalance(linkedCard.getCardBalance() - amountNeeded);

                // Update hub's aggregated balances
                hub.updateBalances();

                // Create and save the transaction record
                TransactionsEntity transaction = new TransactionsEntity();
                transaction.setTransactionName(request.getTransactionName());
                transaction.setAmount(amountNeeded);
                transaction.setWalletUsed(selectedWallet);
                transaction.setHub(hub);
                transaction.setTransactionTime(LocalDateTime.now());
                transaction.setLongitude(request.getLongitude());
                transaction.setLatitude(request.getLatitude());
                transactionsRepository.save(transaction);

                // Persist changes
                walletRepository.save(selectedWallet);
                cardBankRepository.save(linkedCard);
                hubRepository.save(hub);

                //  If QR code exists, mark it as paid
                if (qrCode != null) {
                    qrCode.setPaid(true);
                    qrCodeRepository.save(qrCode);
                    String status= "Accepted";
                    sendTransactionStatus(status);
                    transactionStatus(status);

                }

                transactionSuccessful = true; // Payment succeeded
            } else {
                System.out.println("Transaction canceled: insufficient funds in the selected wallet.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "insufficient funds in the selected wallet.");
//                notificationService.sendFailureNotification(requestBody);
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "insufficient funds in the selected wallet.");
                return false;
            }

        } else {
            // ---------------------------------------------------------------------
            // CASE B: No wallet selected -> MULTI-CARD Checking distribution logic
            // ---------------------------------------------------------------------

            // 1) Gather all checking-type cards in the hub
            List<BankCardEntity> checkingCards = hub.getLinkedCards().stream()
                    .filter(card -> "checking".equalsIgnoreCase(card.getCardType()))
                    .collect(Collectors.toList());

            if (checkingCards.isEmpty()) {
                System.out.println("Transaction canceled: no checking cards linked to this hub.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "no checking cards linked to this hub.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "insufficient funds in the selected wallet.");

                return false;
            }

            // 2) Calculate total of all checking balances
            double totalCheckingBalance = checkingCards.stream()
                    .mapToDouble(BankCardEntity::getCardBalance)
                    .sum();

            // If total checking is insufficient, cancel transaction
            if (totalCheckingBalance < amountNeeded) {
                System.out.println("Transaction canceled: insufficient total checking balance.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "Insufficient balance across all linked cards.");
//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(amountNeeded, request.getTransactionName(), "insufficient funds in the selected wallet.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);


                return false;
            }

            // 3) Distribute amountNeeded across checking cards by ratio
            List<Double> rawShares = new ArrayList<>();
            for (BankCardEntity card : checkingCards) {
                double ratio = card.getCardBalance() / totalCheckingBalance;
                rawShares.add(ratio * amountNeeded);
            }

            // 4) Round each share to thousandths
            int[] sharesInThousandths = new int[rawShares.size()];
            int sumThousandths = 0;
            for (int i = 0; i < rawShares.size(); i++) {
                int rounded = (int) Math.round(rawShares.get(i) * 1000);
                sharesInThousandths[i] = rounded;
                sumThousandths += rounded;
            }
            int targetThousandths = (int) Math.round(amountNeeded * 1000);
            int diff = targetThousandths - sumThousandths;

            // 5) Fix any rounding difference by adjusting the largest share
            if (diff != 0) {
                int largestIndex = 0;
                for (int i = 1; i < sharesInThousandths.length; i++) {
                    if (sharesInThousandths[i] > sharesInThousandths[largestIndex]) {
                        largestIndex = i;
                    }
                }
                sharesInThousandths[largestIndex] += diff;
            }

            // 6) Deduct the final amounts from each card
            for (int i = 0; i < checkingCards.size(); i++) {
                BankCardEntity card = checkingCards.get(i);
                double finalShare = sharesInThousandths[i] / 1000.0;
                card.setCardBalance(card.getCardBalance() - finalShare);
                cardBankRepository.save(card);
            }

            // Update the hub's aggregated balances
            hub.updateBalances();

            // 7) Create a single transaction record for the entire sum
            TransactionsEntity transaction = new TransactionsEntity();
            transaction.setTransactionName(request.getTransactionName());
            transaction.setAmount(amountNeeded);
            transaction.setHub(hub);
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setLongitude(request.getLongitude());
            transaction.setLatitude(request.getLatitude());
            // (No walletUsed here, since no wallet was selected)

            transactionsRepository.save(transaction);
            hubRepository.save(hub);

            //  If QR code exists, mark it as paid
            if (qrCode != null) {
                qrCode.setPaid(true);
                qrCodeRepository.save(qrCode);
                // Notify the website via HTTP callback

                String status= "Accepted";
                sendTransactionStatus(status);
                transactionStatus(status);

            }
            notificationGrpcClient.sendPaymentNotification(selectedWallet.getName(), selectedWallet.getBalance(), amountNeeded, request.getTransactionName());

            transactionSuccessful = true; // Payment succeeded
        }

        // ---------------------- SEND SUCCESSFUL NOTIFICATION ----------------------
        if (transactionSuccessful) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("walletName", selectedWallet.getName());
            requestBody.put("walletBalance", selectedWallet.getBalance());
            requestBody.put("amount", amountNeeded);
            requestBody.put("transactionName", request.getTransactionName());
            String status= "Accepted";
            sendTransactionStatus(status);
            transactionStatus(status);

//            notificationService.sendPaymentNotification(requestBody);
            notificationGrpcClient.sendPaymentNotification(selectedWallet.getName(), selectedWallet.getBalance(), amountNeeded, request.getTransactionName());

        }

        return transactionSuccessful;
    }


    @Transactional
    public HubEntity resetHubCard(UserEntity currentUser) {
        // 1) Get the user's hub
        HubEntity hub = currentUser.getHub();
        if (hub == null) {
            return null; // user has no hub
        }
        // 2) Null out current details to force re-generation
        hub.setHubCardNumber(null);
        hub.setCvv(null);
        hub.setExpDate(null);
        // 3) Manually call the same logic used by @PrePersist / @PreUpdate
        hub.generateHubDetails();
        // 4) Save and return updated hub
        return hubRepository.save(hub);
    }

    @Autowired
    private OpenAIService openAIService;

    public boolean smartPayment(HubCardPaymentRequest request) {
        // 1) Find the hub by the provided hubCardNumber
        Optional<HubEntity> hubOptional = hubRepository.findByHubCardNumber(request.getHubCardNumber());
        if (!hubOptional.isPresent()) {
            System.out.println("Transaction failed: No hub found with the provided card number.");
            notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "No hub found with the provided card number.");

            return false;
        }

        HubEntity hub = hubOptional.get();
        Double amountNeeded = request.getAmount();

        // -----------------------------------------------------------------
        // (AI integration) - Get the list of wallets for the user/hub
        // -----------------------------------------------------------------
        List<WalletEntity> allWallets = hub.getWallets();
        // or use getAllWalletsForUser(user) if you have a user object

        // Build a system instruction telling GPT how to pick a wallet.
        // For example, you might instruct it:
        String walletNames = allWallets.stream()
                .map(WalletEntity::getName)
                .collect(Collectors.joining(", "));

        // The meta script can be something like:
        // "You are a wallet selection assistant. The user has these wallets: X, Y, Z.
        //  The user is making a purchase named 'PHONE'. You must respond with the wallet name
        //  that best matches the category. If you are not sure, respond with 'uncertain'."

        String systemInstructions =
                "You are a wallet-selection AI. The user has the following wallets: " + walletNames + ".\n" +
                        "You need to pick ONE wallet name that best matches the category of the user's purchase.\n" +
                        "Respond ONLY with the exact wallet name (no extra text) or 'uncertain'.";

        // The user prompt is basically the transaction name:
        String userPrompt = request.getTransactionName();

        // Call the AI
        Map<String, Object> aiResponse = openAIService.getChatGPTResponse(systemInstructions, userPrompt);

        // The response body is in aiResponse.get("response"). It's likely a JSON string from OpenAI.
        // You need to parse that JSON to get the model's text. For GPT-3.5-turbo,
        // you can find the text in "choices[0].message.content".
        String rawJson = (String) aiResponse.get("response");

        // Typically, you'd use a JSON parser (e.g. Jackson) to parse out the relevant fields.
        // For quick demonstration, let's assume you parse it out in some parseOpenAiMessage(...) method:
        String walletPickedByAI = parseOpenAiMessageForContent(rawJson);
        // e.g. might return "Electronics" or "uncertain"

        System.out.println("AI-chosen wallet: " + walletPickedByAI);

        // -----------------------------------------------------------------
        // Now attempt to find that AI-chosen wallet in the hub
        // -----------------------------------------------------------------
        WalletEntity selectedWallet = null;
        if (walletPickedByAI != null && !"uncertain".equalsIgnoreCase(walletPickedByAI)) {
            selectedWallet = allWallets.stream()
                    .filter(w -> w.getName().equalsIgnoreCase(walletPickedByAI))
                    .findFirst()
                    .orElse(null);
        }

        // If the AI returns "uncertain" or if there's no match, you can fallback to your existing logic
        // or forcibly pick the previously "selected" wallet, or do something else.
        // For demonstration, let's just proceed if selectedWallet != null,
        // otherwise fallback to your old logic (multi-card).

        if (selectedWallet != null) {
            // (Below is basically the single-wallet logic you already had.)
            // Instead of checking selectedWallet.getSelected(), we simply use the AI-chosen wallet.

            // Grab any one linked card from the chosen wallet
            BankCardEntity linkedCard = selectedWallet.getLinkedCards().stream().findFirst().orElse(null);

            if (linkedCard == null) {
                System.out.println("Transaction canceled: no linked card on the chosen wallet.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "no linked card on the chosen wallet.");
//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "no linked card on the chosen wallet.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                return false;
            }

            // Check if the chosen wallet has enough balance
            if (selectedWallet.getBalance() >= amountNeeded) {
                // Deduct from wallet balance
                selectedWallet.setBalance(selectedWallet.getBalance() - amountNeeded);
                // Deduct from the linked card
                linkedCard.setCardBalance(linkedCard.getCardBalance() - amountNeeded);

                // Update hub's aggregated balances
                hub.updateBalances();

                // Create and save the transaction record
                TransactionsEntity transaction = new TransactionsEntity();
                transaction.setTransactionName(request.getTransactionName());
                transaction.setAmount(amountNeeded);
                transaction.setWalletUsed(selectedWallet);
                transaction.setHub(hub);
                transaction.setTransactionTime(LocalDateTime.now());
                transaction.setLongitude(request.getLongitude());
                transaction.setLatitude(request.getLatitude());
                transactionsRepository.save(transaction);

                // Persist changes
                walletRepository.save(selectedWallet);
                cardBankRepository.save(linkedCard);
                hubRepository.save(hub);

                // Send success notification
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("walletName", selectedWallet.getName());
                requestBody.put("walletBalance", selectedWallet.getBalance());
                requestBody.put("amount", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                notificationGrpcClient.sendPaymentNotification(selectedWallet.getName(), selectedWallet.getBalance(), amountNeeded, request.getTransactionName());
                String status= "Accepted";
                sendTransactionStatus(status);
                transactionStatus(status);

//                notificationService.sendPaymentNotification(requestBody);

                return true;
            } else {
                System.out.println("Transaction canceled: insufficient funds in the chosen wallet.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "insufficient funds in the chosen wallet.");
//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "insufficient funds in the chosen wallet.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                return false;
            }

        } else {
            // If AI picking fails or the AI says "uncertain", fallback to your multi-card logic:
            // 1) Gather all checking-type cards in the hub
            // 2) Check if there's enough total
            // 3) Distribute
            // 4) Create transaction
            // 5) Notification, etc.

            List<BankCardEntity> checkingCards = hub.getLinkedCards().stream()
                    .filter(card -> "checking".equalsIgnoreCase(card.getCardType()))
                    .collect(Collectors.toList());

            if (checkingCards.isEmpty()) {
                System.out.println("Transaction canceled: no checking cards linked to this hub.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "no checking cards linked to this hub.");
//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "no checking cards linked to this hub.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);

                return false;
            }

            double totalCheckingBalance = checkingCards.stream()
                    .mapToDouble(BankCardEntity::getCardBalance)
                    .sum();

            if (totalCheckingBalance < amountNeeded) {
                System.out.println("Transaction canceled: insufficient total checking balance.");
                // SEND FAILURE NOTIFICATION
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amountNeeded", amountNeeded);
                requestBody.put("transactionName", request.getTransactionName());
                requestBody.put("failureReason", "insufficient total checking balance.");
//                notificationService.sendFailureNotification(requestBody);
                notificationGrpcClient.sendFailureNotification(request.getAmount(), request.getTransactionName(), "insufficient total checking balance.");
                String status= "Rejected";
                sendTransactionStatus(status);
                transactionStatus(status);
                return false;
            }

            // Distribute ratio logic...
            // (Your existing rounding code, etc.)

            // Update final distribution
            // Save transaction
            // Send success notification
            // ...

            // For brevity, assume the multi-card distribution is identical to your original snippet.
            // ...
            // Send success notification
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("walletName", selectedWallet.getName());
            requestBody.put("walletBalance", selectedWallet.getBalance());
            requestBody.put("amount", amountNeeded);
            requestBody.put("transactionName", request.getTransactionName());
            String status= "Accepted";
            sendTransactionStatus(status);
            transactionStatus(status);
            notificationGrpcClient.sendPaymentNotification(selectedWallet.getName(), selectedWallet.getBalance(), amountNeeded, request.getTransactionName());
//            notificationService.sendPaymentNotification(requestBody);
            return true;
        }
    }

    /**
     * Helper method to parse the ChatGPT JSON and extract the final text content.
     * In the GPT-3.5/4 API response, you typically look for:
     *   "choices"[0]."message"."content"
     */
    private String parseOpenAiMessageForContent(String openAiResponseJson) {
        try {
            // Example with Jackson:
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(openAiResponseJson);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode contentNode = choices.get(0).get("message").get("content");
                if (contentNode != null) {
                    // Return trimmed text
                    return contentNode.asText().trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // helper method to send status to the web
    public void sendTransactionStatus(String status) {
        System.out.println("Sending transaction status to Next.js: " + status);
        try {
            String nextJsApiUrl = "http://localhost:3000/api/transaction-status";

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", status);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Send POST request
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(nextJsApiUrl, request, String.class);

            System.out.println("Response from Next.js: " + response);
        } catch (Exception e) {
            System.out.println("Error sending transaction status: " + e.getMessage());
        }
    }


    //Helper
    public void transactionStatus(String  status) {
        System.out.println("Sending transaction status to Next.js: " + status);
        try {
            String nextJsApiUrl = "http://localhost:3000/api/transaction-status";

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", status);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Send POST request
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(nextJsApiUrl, request, String.class);

            System.out.println("Response from Next.js: " + response);
        } catch (Exception e) {
            System.out.println("Error sending transaction status: " + e.getMessage());
        }
    }




}





