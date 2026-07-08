package com.igt.spincoreengine.api.model.request;

import com.igt.spincoreengine.utils.ErrorMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Request body for depositing funds into a player's account")
public class DepositRequest implements Serializable {

    @Schema(description = "Amount to deposit. Must be greater than 0.", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = ErrorMessages.AMOUNT_NOT_NULL)
    @DecimalMin(value = "0.01", message = ErrorMessages.AMOUNT_MIN)
    private BigDecimal amount;

    public DepositRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "DepositRequest{" +
                "amount=" + amount +
                '}';
    }
}
