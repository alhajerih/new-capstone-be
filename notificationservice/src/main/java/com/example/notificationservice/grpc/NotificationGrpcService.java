package com.example.notificationservice.grpc;

import com.example.notificationservice.entity.ExpoToken;
import com.example.notificationservice.repository.ExpoTokenRepository;
import com.example.notificationservice.service.NotificationService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.Optional;

@GrpcService
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationService notificationService;
    private final ExpoTokenRepository expoTokenRepository;

    public NotificationGrpcService(NotificationService notificationService, ExpoTokenRepository expoTokenRepository) {
        this.notificationService = notificationService;
        this.expoTokenRepository = expoTokenRepository;
    }

    @Override
    public void registerToken(RegisterTokenRequest request, StreamObserver<RegisterTokenResponse> responseObserver) {
        String token = request.getToken();
        long userId = request.getUserId();

        Optional<ExpoToken> existingToken = expoTokenRepository.findByUserId(userId);
        String responseMessage;

        if (existingToken.isPresent()) {
            existingToken.get().setToken(token);
            expoTokenRepository.save(existingToken.get());
            responseMessage = "Token updated successfully";
        } else {
            ExpoToken newToken = new ExpoToken();
            newToken.setToken(token);
            newToken.setUserId(userId);
            expoTokenRepository.save(newToken);
            responseMessage = "Token registered successfully";
        }

        responseObserver.onNext(RegisterTokenResponse.newBuilder().setMessage(responseMessage).build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendPaymentNotification(PaymentNotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
        Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();
        System.out.println("hello there ");
        System.out.println(request);
        if (expoTokenOptional.isPresent()) {
            String expoPushToken = expoTokenOptional.get().getToken();
            // Format amount and balance to two decimal places
            String formattedAmount = String.format("%.2f", request.getAmount());
            String formattedBalance = String.format("%.2f", request.getBalance());

            String message = String.format(
                    "💳 Payment of %s KD made at %s from wallet '%s'. Your updated balance is %s KD.",
                    formattedAmount, request.getTransactionName(), request.getWallet(), formattedBalance
            );



            notificationService.sendPushNotification(expoPushToken, message);

            responseObserver.onNext(NotificationResponse.newBuilder().setMessage("Success").build());
        } else {
            responseObserver.onNext(NotificationResponse.newBuilder().setMessage("No token found").build());
        }

        responseObserver.onCompleted();
    }



    @Override
    public void sendFailureNotification(FailureNotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
        Optional<ExpoToken> expoTokenOptional = expoTokenRepository.findTopByOrderByIdDesc();

        if (expoTokenOptional.isPresent()) {
            String expoPushToken = expoTokenOptional.get().getToken();
            String message = String.format(
                    "🚫 Transaction Declined! %s | %s KD at %s. Check balance or contact support if needed.",
                    request.getReason(), request.getAmount(), request.getTransactionName()
            );

            notificationService.sendPushNotification(expoPushToken, message);

            NotificationResponse response = NotificationResponse.newBuilder()
                    .setMessage("⚠️ Failure notification sent successfully")
                    .build();
            responseObserver.onNext(response);
        } else {
            NotificationResponse response = NotificationResponse.newBuilder()
                    .setMessage("❌ No Expo Push Token found for store owner")
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

}
