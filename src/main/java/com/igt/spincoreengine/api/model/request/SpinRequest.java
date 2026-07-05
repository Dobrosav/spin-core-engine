package com.igt.spincoreengine.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Schema(description = "Request body for playing a spin")
public class SpinRequest implements Serializable {
    @Schema(description = "Bet amount. Currently only 5 is accepted.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
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
