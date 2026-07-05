package com.igt.spincoreengine.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Response after a successful deposit")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepositResponse implements Serializable {

    @Schema(description = "Updated balance after the deposit", example = "150.00")
    private BigDecimal newBalance;

    public DepositResponse() {
    }

    public DepositResponse(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    @Override
    public String toString() {
        return "DepositResponse{" +
                "newBalance=" + newBalance +
                '}';
    }
}