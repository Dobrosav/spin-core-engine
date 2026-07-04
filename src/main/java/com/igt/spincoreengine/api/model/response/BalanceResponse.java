package com.igt.spincoreengine.api.model.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class BalanceResponse implements Serializable {

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
