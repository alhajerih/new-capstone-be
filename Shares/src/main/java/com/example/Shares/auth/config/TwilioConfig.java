package com.example.Shares.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.AccountSID}")
    private String accountSid;

    @Value("${twilio.AuthToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String phoneNumber;

    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
