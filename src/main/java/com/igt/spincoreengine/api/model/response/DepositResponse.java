package com.igt.spincoreengine.api.model.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class DepositResponse implements Serializable {

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