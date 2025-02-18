package com.example.Shares.OpenAi.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {

    private final String API_URL = "https://api.openai.com/v1/chat/completions";

    // Ensure the property name here matches your configuration (e.g., application.properties)
    @Value("${openai.api.key:default}")
    private String API_KEY;
//
//    public Map<String, Object> getChatGPTResponse(String prompt) {
//        String instructions = "";
//
//        System.out.println("API KEY: " + API_KEY);
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Prepare HTTP headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + API_KEY);
//        headers.set("Content-Type", "application/json");
//
//        // Build the request body with a supported model and the prompt as the user message
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", "gpt-3.5-turbo");
//        requestBody.put("messages", new Object[]{
//                Map.of("role", "system", "content", instructions),
//                Map.of("role", "user", "content", prompt)
//
//        });
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        // Send the POST request to OpenAI
//        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
//
//        // Wrap the API's response body in a JSON-friendly Map
//        Map<String, Object> result = new HashMap<>();
//        result.put("response", response.getBody());
//        return result;
//    }
public Map<String, Object> getChatGPTResponse(String systemInstructions, String userPrompt) {
    System.out.println("API KEY: " + API_KEY);
    RestTemplate restTemplate = new RestTemplate();

    // Prepare HTTP headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + API_KEY);
    headers.set("Content-Type", "application/json");

    // Build the request body with a supported model and the prompt as the user message
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-3.5-turbo");
    requestBody.put("messages", new Object[]{
            Map.of("role", "system", "content", systemInstructions),
            Map.of("role", "user", "content", userPrompt)
    });

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    // Send the POST request to OpenAI
    ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

    // Wrap the API's response body in a JSON-friendly Map
    Map<String, Object> result = new HashMap<>();
    result.put("response", response.getBody());
    return result;
}

}
