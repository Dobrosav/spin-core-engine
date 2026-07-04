package com.igt.spincoreengine.api.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class SpinRequest implements Serializable {
    @NotNull(message = "Bet amount cannot be null")
    @Min(value = 5, message = "Bet amount must be equal to 5")
    @Max(value = 5, message = "Bet amount must be equal to 5")
    private Integer betAmount;

    public SpinRequest() {
    }

    public Integer getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(Integer betAmount) {
        this.betAmount = betAmount;
    }

    @Override
    public String toString() {
        return "SpinRequest{" +
                "betAmount=" + betAmount +
                '}';
    }
}
