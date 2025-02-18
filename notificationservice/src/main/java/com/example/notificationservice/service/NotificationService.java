package com.example.notificationservice.service;


import com.example.notificationservice.entity.ExpoToken;
import com.example.notificationservice.repository.ExpoTokenRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationService {
    private static final  String  EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final ExpoTokenRepository expoTokenRepository;
    public NotificationService(ExpoTokenRepository expoTokenRepository) {
        this.expoTokenRepository = expoTokenRepository;
    }

    public void sendPushNotification(String expoPushToken,String message){
        if(!expoPushToken.startsWith("ExponentPushToken")){
            System.out.println("Invalid Expo Push Token");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        body.put("to",expoPushToken);
        body.put("title","Pockets");
        body.put("body",message);
        body.put("sound","default");

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body,headers);
        String response = restTemplate.postForObject(EXPO_PUSH_URL,entity,String.class);
        System.out.println("Expo push Response: "+ response);
    }

    public void sendPaymentNotification( String wallet,Double balance,Double amount, String transactionName) {

            // Get Expo Push Token of the user
            Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();

            if (expoTokenOptional.isPresent()) {
                String expoPushToken = expoTokenOptional.get().getToken();

                // Format amount and balance to two decimal places
                String formattedAmount = String.format("%.2f", amount);
                String formattedBalance = String.format("%.2f", balance);


                String message = "You spent " + formattedAmount + " KD at " + transactionName +" From your wallet: "+wallet+". The new balance is : "+formattedBalance+" KD";

                sendPushNotification(expoPushToken, message);
                System.out.println("Notification sent: " + message);
            } else {
                System.out.println("No Expo Push Token found for this user.");
            }
        }


    public void sendFailureNotification( Double amount, String transactionName, String reason) {

            // Get Expo Push Token of the store owner
            Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();

            if (expoTokenOptional.isPresent()) {
                String expoPushToken = expoTokenOptional.get().getToken();
                String message = "Transaction declined: " + reason + ". Amount: " + amount + " KD at " + transactionName;

                sendPushNotification(expoPushToken, message);
                System.out.println("Failure notification sent: " + message);
            } else {
                System.out.println("No Expo Push Token found for this store owner.");
            }
        }



    }






