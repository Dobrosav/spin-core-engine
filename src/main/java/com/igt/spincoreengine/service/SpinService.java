package com.igt.spincoreengine.service;

import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.model.WinningLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SpinService {

    private static final Logger logger = LoggerFactory.getLogger(SpinService.class);

    private static final String[] REEL_STRIP = {"Q", "A", "K", "W", "W", "A", "Q", "K", "K", "W"};

    private final PlayerService playerService;

    private static final int[][][] PAYLINES = {
            {{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}}, // Line 1: Top Horizontal[cite: 1]
            {{1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}}, // Line 2: Middle Horizontal[cite: 1]
            {{2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}}, // Line 3: Bottom Horizontal[cite: 1]
            {{0, 0}, {1, 1}, {2, 2}, {1, 3}, {0, 4}}, // Line 4: V-Shape[cite: 1]
            {{2, 0}, {1, 1}, {0, 2}, {1, 3}, {2, 4}}  // Line 5: Inverted V-Shape[cite: 1]
    };

    public SpinService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public SpinResponse playSpin(Long playerId, Integer betAmount) {

        List<List<String>> matrix = generateMatrix();

        List<WinningLine> winningLines = new ArrayList<>();
        BigDecimal totalWin = BigDecimal.ZERO;

        for (int i = 0; i < PAYLINES.length; i++) {
            WinningLine win = evaluateSingleLine(PAYLINES[i], i + 1, matrix);
            if (win != null) {
                winningLines.add(win);
                totalWin = totalWin.add(win.getPayout());
            }
        }

        BigDecimal newBalance = playerService.updateBalanceForSpin(playerId, BigDecimal.valueOf(betAmount), totalWin);

        return new SpinResponse(matrix, winningLines, totalWin, newBalance);
    }

    protected List<List<String>> generateMatrix() {
        List<List<String>> matrix = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            matrix.add(new ArrayList<>());
        }

        for (int col = 0; col < 5; col++) {
            int stopPosition = ThreadLocalRandom.current().nextInt(REEL_STRIP.length);

            for (int row = 0; row < 3; row++) {
                // Wrap-around logika: Modulo operator osigurava da se vratimo na početak niza[cite: 1]
                int index = (stopPosition + row) % REEL_STRIP.length;
                matrix.get(row).add(REEL_STRIP[index]);
            }
        }
        return matrix;
    }

    private WinningLine evaluateSingleLine(int[][] lineCoordinates, int lineId, List<List<String>> matrix) {
        String[] lineSymbols = new String[5];
        for (int i = 0; i < 5; i++) {
            int row = lineCoordinates[i][0];
            int col = lineCoordinates[i][1];
            lineSymbols[i] = matrix.get(row).get(col);
        }

        String targetSymbol = null;
        int matchCount = 0;

        for (int i = 0; i < 5; i++) {
            if (targetSymbol == null && !lineSymbols[i].equals("W")) {
                targetSymbol = lineSymbols[i];
            }
        }


        if (targetSymbol == null) {
            targetSymbol = "A";
        }

        for (int i = 0; i < 5; i++) {
            if (lineSymbols[i].equals(targetSymbol) || lineSymbols[i].equals("W")) {
                matchCount++;
            } else {
                break;
            }
        }

        if (matchCount >= 3) {
            BigDecimal payout = calculatePayout(targetSymbol, matchCount);
            return new WinningLine(lineId, targetSymbol, payout);
        }

        return null;
    }

    private BigDecimal calculatePayout(String symbol, int matchCount) {
        double multiplier = 0;

        switch (symbol) {
            case "A":
                if (matchCount == 5) multiplier = 10;
                else if (matchCount == 4) multiplier = 5;
                else if (matchCount == 3) multiplier = 2;
                break;
            case "K":
                if (matchCount == 5) multiplier = 5;
                else if (matchCount == 4) multiplier = 3;
                else if (matchCount == 3) multiplier = 1;
                break;
            case "Q":
                if (matchCount == 5) multiplier = 2;
                else if (matchCount == 4) multiplier = 1;
                else if (matchCount == 3) multiplier = 0.5;
                break;
        }

        return BigDecimal.valueOf(multiplier);
    }
}
