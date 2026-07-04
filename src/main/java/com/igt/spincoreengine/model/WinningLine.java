package com.igt.spincoreengine.model;

import java.math.BigDecimal;

public class WinningLine {

    private int lineId;
    private String winningSymbol;
    private BigDecimal payout;

    public WinningLine() {
    }

    public WinningLine(int lineId, String winningSymbol, BigDecimal payout) {
        this.lineId = lineId;
        this.winningSymbol = winningSymbol;
        this.payout = payout;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getWinningSymbol() {
        return winningSymbol;
    }

    public void setWinningSymbol(String winningSymbol) {
        this.winningSymbol = winningSymbol;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }
}