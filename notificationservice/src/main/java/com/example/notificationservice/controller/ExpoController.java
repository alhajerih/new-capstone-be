package com.example.notificationservice.controller;

import com.example.notificationservice.entity.ExpoToken;
import com.example.notificationservice.repository.ExpoTokenRepository;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.WebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup")
public class ExpoController {

    @Autowired
    private final ExpoTokenRepository expoTokenRepository;

    @Autowired

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final WebSocketHandler webSocketHandler;
    public ExpoController(ExpoTokenRepository expoTokenRepository, ObjectMapper objectMapper, NotificationService notificationService, WebSocketHandler webSocketHandler) {
        this.expoTokenRepository = expoTokenRepository;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/notification")
    public String registerToken(@RequestBody String requestBody) {
        System.out.println("requestBody: " + requestBody);

        try {
            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            // Extract token
            String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
            if (token == null || token.isEmpty()) {
                return "Invalid request: Token is missing";
            }

            // Extract userID safely
            Long userId = (jsonNode.has("userId") && !jsonNode.get("userId").isNull()) ? jsonNode.get("userId").asLong() : null;
            if (userId == null) {
                return "Invalid request: User ID is missing";
            }

            // Check if the token is already registered
            Optional<ExpoToken> existingTokenOptional = expoTokenRepository.findByToken(token);

            if (existingTokenOptional.isPresent()) {
                ExpoToken existingToken = existingTokenOptional.get();

                // If the token belongs to the same user, do nothing
                if (existingToken.getUserId().equals(userId)) {
                    return "Token already registered for this user, no changes made.";
                } else {
                    // If the token belongs to a different user, update it
                    existingToken.setUserId(userId);
                    expoTokenRepository.save(existingToken);
                    return "Token ownership updated to the new user";
                }
            }

            // Check if the user already has a registered token
            Optional<ExpoToken> userTokenOptional = expoTokenRepository.findByUserId(userId);
            if (userTokenOptional.isPresent()) {
                ExpoToken userToken = userTokenOptional.get();

                // If the token is different, update it
                if (!userToken.getToken().equals(token)) {
                    userToken.setToken(token);
                    expoTokenRepository.save(userToken);
                    return "User's token updated successfully";
                } else {
                    return "Token already registered for this user, no changes made.";
                }
            }

            // If neither token nor user exists, create a new record
            ExpoToken newToken = new ExpoToken();
            newToken.setToken(token);
            newToken.setUserId(userId);
            expoTokenRepository.save(newToken);
            return "Token registered successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing request: " + e.getMessage();
        }
    }


    @PostMapping("/sendPaymentNotification")
    public void sendPaymentNotification(@RequestBody Map<String, Object> requestBody) {
        // Extract values from the JSON request body
        String wallet = (String) requestBody.get("walletName");
        double balance = Double.parseDouble(requestBody.get("walletBalance").toString());
        double amount = Double.parseDouble(requestBody.get("amount").toString());
        String transactionName = (String) requestBody.get("transactionName");

        // Get Expo Push Token of the user
        Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();

        if (expoTokenOptional.isPresent()) {
            String expoPushToken = expoTokenOptional.get().getToken();

            // Format amount and balance to two decimal places
            String formattedAmount = String.format("%.2f", amount);
            String formattedBalance = String.format("%.2f", balance);

            String message = "You spent " + formattedAmount + " KD at " + transactionName +
                    " from your wallet: " + wallet + ". The new balance is: " + formattedBalance + " KD";

            notificationService.sendPushNotification(expoPushToken, message);
            System.out.println("Notification sent: " + message);
        }
    }



    @PostMapping("/sendFailureNotification")
    public void sendFailureNotification (@RequestBody Map<String, Object> requestBody) {
        //String amount,String transactionName,String reason
        System.out.println("requestBody: " + requestBody);
        // Extract values from the JSON request body
        double amount = Double.parseDouble(requestBody.get("amountNeeded").toString());
        String reason = (String) requestBody.get("failureReason");
        String transactionName = (String) requestBody.get("transactionName");

        // Get Expo Push Token of the store owner
        Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();

        if (expoTokenOptional.isPresent()) {
            String expoPushToken = expoTokenOptional.get().getToken();
            String message = "Transaction declined: " + reason + ". Amount: " + amount + " KD at " + transactionName;

            notificationService.sendPushNotification(expoPushToken, message);
            System.out.println("Failure notification sent: " + message);
        } else {
            System.out.println("No Expo Push Token found for this store owner.");
        }
    }

}
