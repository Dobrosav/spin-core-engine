package com.igt.spincoreengine.api.model.request;

import com.igt.spincoreengine.utils.ErrorMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Schema(description = "Request body for playing a spin")
public class SpinRequest implements Serializable {

    @Schema(description = "Bet amount. Currently only 5 is accepted.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = ErrorMessages.BET_AMOUNT_NOT_NULL)
    @Min(value = 5, message = ErrorMessages.BET_AMOUNT_EQUAL_TO_5)
    @Max(value = 5, message = ErrorMessages.BET_AMOUNT_EQUAL_TO_5)
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

