package com.igt.spincoreengine.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Response containing the player's current balance")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceResponse implements Serializable {

    @Schema(description = "Current balance of the player", example = "100.00")
    private BigDecimal balance;

    public BalanceResponse() {
    }

    public BalanceResponse(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "BalanceResponse{" +
                "balance=" + balance +
                '}';
    }

}
