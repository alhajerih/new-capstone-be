package com.example.Shares.auth.service;

import com.example.Shares.auth.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    private final TwilioConfig twilioConfig;

    @Autowired
    public TwilioService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;

        // Initialize Twilio with account SID and auth token
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    public void sendSms(String toPhoneNumber, String otp) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),  // To phone number
                    new PhoneNumber(twilioConfig.getPhoneNumber()),  // From Twilio phone number
                    "Your OTP is: " + otp  // Message body
            ).create();

            System.out.println("Message sent successfully! SID: " + message.getSid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }
}
