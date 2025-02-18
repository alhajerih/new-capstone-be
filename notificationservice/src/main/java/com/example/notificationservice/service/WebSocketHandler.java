package com.example.notificationservice.service;

import com.example.notificationservice.entity.ExpoToken;
import com.example.notificationservice.repository.ExpoTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ExpoTokenRepository expoTokenRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    //Constructor
    public WebSocketHandler(ExpoTokenRepository expoTokenRepository, NotificationService notificationService, ObjectMapper objectMapper) {
        this.expoTokenRepository = expoTokenRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void  handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String payload = message.getPayload();

        //Retrieve the expo push token from the table
        Optional<ExpoToken> tokenEntry= expoTokenRepository.findTopByOrderByIdDesc();


        if(tokenEntry.isPresent()){
            String expoPushToken = tokenEntry.get().getToken();
            notificationService.sendPushNotification(expoPushToken,payload);
            if(expoPushToken == null || expoPushToken.isEmpty()){
                System.out.println("No valid Expo Push Token found");
            }else{
                System.out.println("Sending push notification to token: "+expoPushToken);
            }


            //Broadcast via WebSocket
            for(WebSocketSession s : sessions){
                if (s.isOpen()){
                    s.sendMessage(new TextMessage("Notification has been sent"));
                }
            }
        }
    }

    // Close the session of WebSocket
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        sessions.remove(session);
        System.out.println("Client disconnected: "+session.getId());
    }

}
