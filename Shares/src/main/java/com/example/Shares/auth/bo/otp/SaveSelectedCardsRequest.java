package com.example.Shares.auth.bo.otp;

import java.util.List;

public class SaveSelectedCardsRequest {
    private String otp;
    private List<Long> selectedCardIds;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public List<Long> getSelectedCardIds() {
        return selectedCardIds;
    }

    public void setSelectedCardIds(List<Long> selectedCardIds) {
        this.selectedCardIds = selectedCardIds;
    }
}
