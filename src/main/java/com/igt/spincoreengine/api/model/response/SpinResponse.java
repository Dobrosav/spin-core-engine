package com.igt.spincoreengine.api.model.response;

import com.igt.spincoreengine.model.WinningLine;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


public class SpinResponse implements Serializable {

    private List<List<String>> matrix;
    private List<WinningLine> winningLines;
    private BigDecimal totalWin;
    private BigDecimal newBalance;

    public SpinResponse() {
    }

    public SpinResponse(List<List<String>> matrix, List<WinningLine> winningLines, BigDecimal totalWin, BigDecimal newBalance) {
        this.matrix = matrix;
        this.winningLines = winningLines;
        this.totalWin = totalWin;
        this.newBalance = newBalance;
    }

    public List<List<String>> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<List<String>> matrix) {
        this.matrix = matrix;
    }

    public List<WinningLine> getWinningLines() {
        return winningLines;
    }

    public void setWinningLines(List<WinningLine> winningLines) {
        this.winningLines = winningLines;
    }

    public BigDecimal getTotalWin() {
        return totalWin;
    }

    public void setTotalWin(BigDecimal totalWin) {
        this.totalWin = totalWin;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    @Override
    public String toString() {
        return "SpinResponse{" +
                "matrix=" + matrix +
                ", winningLines=" + winningLines +
                ", totalWin=" + totalWin +
                ", newBalance=" + newBalance +
                '}';
    }
}
