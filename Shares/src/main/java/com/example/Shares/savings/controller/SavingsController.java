package com.example.Shares.savings.controller;

import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.service.UserService;  // Example
import com.example.Shares.savings.bo.CreateSavingsRequest;
import com.example.Shares.savings.entity.SavingsEntity;
import com.example.Shares.savings.service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class SavingsController {

    @Autowired
    private SavingsService savingsService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-savings")
    public ResponseEntity<Map<String, Object>> createSavingsGoal(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateSavingsRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String jwt = token.substring(7);
            UserEntity currentUser = userService.getUserFromToken(jwt);

            SavingsEntity savings = savingsService.createSavingsGoal(currentUser, request);

            response.put("httpStatus", 201);
            response.put("message", "Savings goal created successfully.");
            response.put("savingsId", savings.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("httpStatus", 400);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{savingsId}/pay")
    public ResponseEntity<Map<String, Object>> processMonthlyPayment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long savingsId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String jwt = token.substring(7);
            UserEntity currentUser = userService.getUserFromToken(jwt);

            SavingsEntity updated = savingsService.processMonthlyPayment(savingsId);

            response.put("httpStatus", 200);
            response.put("message", "Monthly payment processed.");
            response.put("paymentsMade", updated.getPaymentsMade());
            response.put("totalMonths", updated.getMonths());
            response.put("active", updated.getActive());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("httpStatus", 400);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
