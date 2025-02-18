package com.example.Shares.auth.service;

import com.example.Shares.auth.bo.LoginResponse;
import com.example.Shares.auth.bo.otp.GenerateOtpResponse;
import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<BankCardEntity> getBankCards(String civilId);

    LoginResponse registerUser(String civilId, String username, String password, MultipartFile profilePicture);

    String validateOtp(String otp);
    ResponseEntity<Resource> getProfilePicture(UserEntity token);    GenerateOtpResponse generateOtp(String civilId);
    String savePicture(UserEntity user,MultipartFile file);
    void saveSelectedCards(String token, List<Long> selectedCardIds);

    List<BankCardEntity> getLinkedCards(String token);

    UserEntity getUserFromToken(String token);

    String login(String username, String password);
}


