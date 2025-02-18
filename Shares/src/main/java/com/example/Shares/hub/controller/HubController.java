package com.example.Shares.hub.controller;

import com.example.Shares.QRcode.QRCodeEntity;
import com.example.Shares.QRcode.QRCodeService;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.service.UserService;
import com.example.Shares.hub.bo.CardThemeRequest;
import com.example.Shares.hub.bo.HubCardPaymentRequest;
import com.example.Shares.hub.bo.PaymentDoneResponse;
import com.example.Shares.hub.bo.PaymentRequest;
import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.hub.repository.HubRepository;
import com.example.Shares.hub.service.HubService;
import com.example.Shares.transactions.entity.TransactionsEntity;
import com.example.Shares.transactions.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class HubController {

    private final String websiteCallbackUrl = "https://your-website.com/api/transaction-status";


    @Autowired
    private UserService userService;
    @Autowired
    private HubService hubService;
    @Autowired
    private HubRepository hubRepository;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<String> processPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7); // Remove "Bearer " prefix

        UserEntity currentUser = userService.getUserFromToken(jwt);
        boolean success = hubService.processPaymentWithChecking(currentUser, request);

        if (success) {
            return ResponseEntity.ok("Transaction successful and recorded.");
        } else {
            return ResponseEntity.badRequest().body("Transaction failed due to insufficient funds or other issues.");
        }
    }
    @PostMapping("/pay-with-hubcard")
    public ResponseEntity<String> payWithHubCard(@RequestBody HubCardPaymentRequest request) {
        boolean success = hubService.processPaymentByHubCard(request);

        if (success) {
            return ResponseEntity.ok("Payment successful and recorded.");
        } else {
            return ResponseEntity.badRequest().body("Payment failed. Please check details and try again.");
        }
    }

    @PostMapping("/reset-card")
    public ResponseEntity<?> resetHubCard(
            @RequestHeader("Authorization") String token
    ) {
        try {
            // 1) Parse the JWT from "Bearer ..."
            String jwt = token.substring(7);

            // 2) Get the current user from the token
            UserEntity currentUser = userService.getUserFromToken(jwt);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid user token or user not found.");
            }

            // 3) Call the service to reset card details
            HubEntity updatedHub = hubService.resetHubCard(currentUser);

            if (updatedHub == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User does not have a hub to reset.");
            }

            return ResponseEntity.ok("Hub card details have been reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resetting hub card details: " + e.getMessage());
        }
    }

    @PostMapping("/pay-with-hubcard-ai")
    public boolean smartPay(@RequestBody HubCardPaymentRequest request) {
        return hubService.smartPayment(request);
    }

    @PostMapping("/pay-with-hubcard-unified")
    public ResponseEntity<PaymentDoneResponse> unifiedPayWithHubCard(@RequestBody HubCardPaymentRequest request) {
        PaymentDoneResponse responseBody = new PaymentDoneResponse();

        // 1) Find the hub by hubCardNumber
        Optional<HubEntity> hubOpt = hubRepository.findByHubCardNumber(request.getHubCardNumber());
        if (!hubOpt.isPresent()) {
            responseBody.setMessage("No hub found with that card number.");
            responseBody.setStatus("ERROR");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }

        HubEntity hub = hubOpt.get();

        // 2) Get the user from the hub
        UserEntity user = hub.getUser();
        if (user == null) {
            responseBody.setMessage("No user associated with this hub.");
            responseBody.setStatus("ERROR");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }

        // 3) Check the user's smartPay setting
        boolean isSmartPayEnabled = Boolean.TRUE.equals(user.getSmartPay());

        // 4) Decide which service method to call
        boolean paymentSuccess;
        if (isSmartPayEnabled) {
            paymentSuccess = hubService.smartPayment(request);
        } else {
            paymentSuccess = hubService.processPaymentByHubCard(request);
        }

        // 5) Build the appropriate JSON response
        if (paymentSuccess) {
            responseBody.setMessage("Payment successful and recorded.");
            responseBody.setStatus("SUCCESS");
            return ResponseEntity.ok(responseBody);
        } else {
            responseBody.setMessage("Payment failed. Please check details and try again.");
            responseBody.setStatus("FAILURE");
            return ResponseEntity.badRequest().body(responseBody);
        }
    }


    @GetMapping("/hub-transactions")
    public ResponseEntity<?> getTransactionsForHub(@RequestHeader("Authorization") String token,@RequestParam Long hubId,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {

        String jwt = token.substring(7);
        UserEntity user = userService.getUserFromToken(jwt);

        // Check if the hub exists for the user
        if(!user.getHub().getId().equals(hubId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hub not found");
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<TransactionsEntity> transactions = transactionService.getTransactionByHubId(hubId,pageable);

        return ResponseEntity.ok(transactions);
    }



    // Endpoint to save QR code
    @PostMapping("/qrcode")
    public ResponseEntity<QRCodeEntity> saveQRCode(@RequestBody QRCodeEntity qrCodeEntity) {
        QRCodeEntity savedQRCode = qrCodeService.saveQRCode(qrCodeEntity);
        return ResponseEntity.ok(savedQRCode);
    }

    @PostMapping("/update-card-theme")
    public ResponseEntity<?> updateCardTheme(
            @RequestHeader("Authorization") String token,
            @RequestBody CardThemeRequest request
    ) {
        try {
            String jwt = token.substring(7);
            UserEntity currentUser = userService.getUserFromToken(jwt);
            
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid user token.");
            }

            HubEntity hub = currentUser.getHub();
            if (hub == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hub found for user.");
            }

            // Validate theme number
            if (request.getTheme() < 1 || request.getTheme() > 6) {
                return ResponseEntity.badRequest()
                        .body("Theme must be between 1 and 6.");
            }

            hub.setCardTheme(request.getTheme());
            hubRepository.save(hub);

            // Return card details along with the updated theme
            Map<String, Object> response = new HashMap<>();
            response.put("cardNumber", hub.getHubCardNumber());
            response.put("theme", hub.getCardTheme());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error updating card theme: " + e.getMessage());
        }
    }

    @GetMapping("/card-details")
    public ResponseEntity<?> getCardDetails(
            @RequestHeader("Authorization") String token
    ) {
        try {
            String jwt = token.substring(7);
            UserEntity currentUser = userService.getUserFromToken(jwt);
            
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid user token.");
            }

            HubEntity hub = currentUser.getHub();
            if (hub == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hub found for user.");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("cardNumber", hub.getHubCardNumber());
            response.put("theme", hub.getCardTheme());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error retrieving card details: " + e.getMessage());
        }
    }
}




