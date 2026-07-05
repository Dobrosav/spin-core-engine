package com.igt.spincoreengine.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Represents a single winning payline")
public class WinningLine {

    @Schema(description = "Payline identifier (1=top row, 2=middle row, 3=bottom row, 4=V-shape, 5=inverted V-shape)", example = "1")
    private int lineId;
    @Schema(description = "The symbol that formed the winning combination", example = "A")
    private String winningSymbol;
    @Schema(description = "Payout multiplier for this winning line", example = "10.00")
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