//package com.example.Shares.feign;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import java.util.Map;
//
//@FeignClient(name = "notification-service")
//public interface Notification {
//
//    @PostMapping("/api/setup/notification")
//    public String registerToken(@RequestBody String requestBody);
//
//    @PostMapping("/api/setup/sendPaymentNotification")
//    public void sendPaymentNotification(@RequestBody Map<String, Object> requestBody);
//
//    @PostMapping("/api/setup/sendFailureNotification")
//    public void sendFailureNotification (@RequestBody Map<String, Object> requestBody);
//
//}
