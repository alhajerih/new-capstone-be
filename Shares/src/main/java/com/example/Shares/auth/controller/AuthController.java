package com.example.Shares.auth.controller;

import com.example.Shares.auth.bo.LoginResponse;
import com.example.Shares.auth.bo.SmartPayRequest;
import com.example.Shares.auth.bo.auth.CreateLoginRequest;
import com.example.Shares.auth.bo.otp.*;
import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.repository.UserRepository;
import com.example.Shares.auth.service.UserService;
import com.example.Shares.hub.repository.HubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class AuthController {


    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/generate-otp")
    public ResponseEntity<GenerateOtpResponse> generateOtp(@RequestBody GenerateOtpRequest request) {
        GenerateOtpResponse otp = userService.generateOtp(request.getCivilId());
        return ResponseEntity.ok(otp);
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody ValidateOtpRequest request) {
        String validated = userService.validateOtp(request.getOtp());
        return ResponseEntity.ok(validated);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerUser(
            @RequestParam("civilId") String civilId,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        LoginResponse register = userService.registerUser(civilId, username, password, profilePicture);
        return ResponseEntity.ok(register);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody CreateLoginRequest loginRequest) {
        // Authenticate the user and generate a token
        String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);

        return loginResponse;
    }


    @GetMapping("/bank-cards")
    public ResponseEntity<List<BankCardEntity>> getBankCards(@RequestHeader("Authorization") String authorizationHeader) {
        // Check Authorization Header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        // Extract Token
        String token = authorizationHeader.substring(7); // Remove "Bearer "
        System.out.println("Extracted Token: " + token);

        // Validate and Fetch Bank Cards
        List<BankCardEntity> bankCards = userService.getBankCards(token);
        return ResponseEntity.ok(bankCards);
    }


    @GetMapping("/linked-cards")
    public ResponseEntity<List<BankCardEntity>> getLinkedCards(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract JWT by removing the "Bearer " prefix
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);

        System.out.println("Extracted Token: " + token);

        // Fetch linked cards using the token
        List<BankCardEntity> cards = userService.getLinkedCards(token);

        return ResponseEntity.ok(cards);
    }

    @PostMapping("/select-cards")
    public ResponseEntity<String> saveSelectedCards(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody List<Long> selectedCardIds) {
        // Extract token and pass it to the service
        String token = authorizationHeader.substring(7);
        userService.saveSelectedCards(token, selectedCardIds);
        return ResponseEntity.ok("Selected cards updated successfully.");
    }


    @GetMapping("/user-details")
    public ResponseEntity<UserEntity> getUserDetails(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // Remove "Bearer " prefix
        UserEntity user = userService.getUserFromToken(jwt);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/add-pic")
    public  ResponseEntity<?> addPicture(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file) {
        String jwt = token.substring(7);
        UserEntity user = userService.getUserFromToken(jwt);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user or token.");
        }

        String pictureUrl = userService.savePicture(user,file);
        if (pictureUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload the picture.");
        }
        return ResponseEntity.ok(pictureUrl);
    }
    @GetMapping("/profile-pic")
    public ResponseEntity<Resource> getUserProfilePicture(@RequestHeader("Authorization") String token) {
        // Extract user from token
        String jwt = token.substring(7);
        UserEntity user = userService.getUserFromToken(jwt);

        if (user == null || user.getPictureUrl() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return userService.getProfilePicture(user);
    }

    @PostMapping("/smartpay")
    public ResponseEntity<String> updateSmartPay(
            @RequestHeader("Authorization") String token,
            @RequestBody SmartPayRequest request
    ) {
        // 1. Extract the raw JWT (assuming "Bearer <token>")
        String jwt = token.substring(7);

        // 2. Retrieve the User from the token
        UserEntity user = userService.getUserFromToken(jwt);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user or token.");
        }

        // 3. Update the 'smartPay' field
        user.setSmartPay(request.getSmartPay());

        // 4. Persist changes
        userRepository.save(user);

        return ResponseEntity.ok(
                "User smartPay updated to: " + request.getSmartPay()
        );
    }

}
