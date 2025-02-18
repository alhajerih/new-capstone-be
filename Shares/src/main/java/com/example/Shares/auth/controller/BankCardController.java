package com.example.Shares.auth.controller;

import com.example.Shares.auth.bo.AddCardToHubRequest;
import com.example.Shares.auth.bo.BankCardRequest;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.service.BankCardService;
import com.example.Shares.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class BankCardController {


    @Autowired
    private BankCardService bankCardService;
    @Autowired
    private UserService userService;

    @PostMapping("/create-bank-card")
    public ResponseEntity<String> createNewWallet(@RequestHeader("Authorization") String token, @RequestBody BankCardRequest request) {

        String jwt = token.substring(7); // Remove "Bearer " prefix
        UserEntity user = userService.getUserFromToken(jwt);

        bankCardService.createCard(request, user);
        return ResponseEntity.ok("Card created");
    }

    @PostMapping("/add-card-to-hub")
    public ResponseEntity<String> addCardToHub(@RequestHeader("Authorization") String token, @RequestBody AddCardToHubRequest request) {

        String jwt = token.substring(7); // Remove "Bearer " prefix
        UserEntity user = userService.getUserFromToken(jwt);

        bankCardService.addCardToHub(request, user);
        return ResponseEntity.ok("Card Added");
    }

    @PostMapping("/remove-card-from-hub")
    public ResponseEntity<String> removeCardFromHub(@RequestHeader("Authorization") String token, @RequestBody AddCardToHubRequest request) {

        String jwt = token.substring(7); // Remove "Bearer " prefix
        UserEntity user = userService.getUserFromToken(jwt);

        bankCardService.removeCardFromHub(request, user);
        return ResponseEntity.ok("Card removed");
    }


}




