package com.example.Shares.OpenAi.controller;

import com.example.Shares.OpenAi.bo.ChatRequest;
import com.example.Shares.OpenAi.service.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

//    @PostMapping("/api/v1/user/chat")
//    public ResponseEntity<Map<String, Object>> getChatResponse(@RequestBody Map<String, String> request) {
//        String prompt = request.get("prompt");
//        Map<String, Object> response = openAIService.getChatGPTResponse(prompt);
//        return ResponseEntity.ok(response);
//    }
}
