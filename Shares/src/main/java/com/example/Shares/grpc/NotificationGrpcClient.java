package com.example.Shares.grpc;

import com.example.notificationservice.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class NotificationGrpcClient {

    @GrpcClient("notification-service")
    private  NotificationServiceGrpc.NotificationServiceBlockingStub notificationStub;

    public String sendPaymentNotification(String wallet, double balance, double amount, String transactionName) {
        if (notificationStub == null) {
            System.err.println("❌ gRPC Stub is NULL - Check gRPC Client Configuration!");
            return "Error: gRPC Client not initialized";
        }


        PaymentNotificationRequest request = PaymentNotificationRequest.newBuilder()
                .setWallet(wallet)
                .setBalance(balance)
                .setAmount(amount)
                .setTransactionName(transactionName)
                .build();
        System.out.println("Sending Payment Notification to Notification Service...");
        NotificationResponse response = notificationStub.sendPaymentNotification(request);
        return response.getMessage();
    }


    public String sendFailureNotification(double amount, String transactionName, String reason) {
        if (notificationStub == null) {
            System.err.println("❌ gRPC Stub is NULL - Check gRPC Client Configuration!");
            return "Error: gRPC Client not initialized";
        }

        FailureNotificationRequest request = FailureNotificationRequest.newBuilder()
                .setAmount(amount)
                .setTransactionName(transactionName)
                .setReason(reason)
                .build();
        System.out.println("⚠️ Sending Failure Notification to Notification Service...");
        NotificationResponse response = notificationStub.sendFailureNotification(request);
        return response.getMessage();
    }


}
