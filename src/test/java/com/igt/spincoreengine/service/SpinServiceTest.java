package com.igt.spincoreengine.service;

import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.model.WinningLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpinServiceTest {

    @Mock
    private PlayerService playerService;

    private SpinService spinService;

    @BeforeEach
    void setUp() {
        spinService = spy(new SpinService(playerService));
    }

    private List<List<String>> buildMatrix(String[][] grid) {
        return Arrays.asList(
                Arrays.asList(grid[0]),
                Arrays.asList(grid[1]),
                Arrays.asList(grid[2])
        );
    }

    @Nested
    @DisplayName("Mandatory Scenarios from task.md")
    class MandatoryScenarios {

        @Test
        @DisplayName("Scenario A - Pure Win: Line 1 = [K,K,K,Q,A] → 3-of-a-kind K, payout = 1")
        void scenarioA_pureWin_threeOfAKindK() {
            String[][] grid = {
                    {"K", "K", "K", "Q", "A"},
                    {"Q", "A", "Q", "K", "Q"},
                    {"A", "Q", "A", "A", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            SpinResponse response = spinService.playSpin(1L, 5);

            assertNotNull(response);
            assertFalse(response.getWinningLines().isEmpty(), "Should have at least one winning line");

            WinningLine line1Win = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1)
                    .findFirst()
                    .orElse(null);

            assertNotNull(line1Win, "Line 1 should be a winning line");
            assertEquals("K", line1Win.getWinningSymbol(), "Winning symbol should be K");
            assertEquals(0, new BigDecimal("1").compareTo(line1Win.getPayout()),
                    "3-of-a-kind K payout should be 1");
        }

        @Test
        @DisplayName("Scenario B - Wild Substitution: Line 3 = [A,W,A,A,Q] → 4-of-a-kind A, payout = 5")
        void scenarioB_wildSubstitution_fourOfAKindA() {
            String[][] grid = {
                    {"Q", "K", "Q", "K", "Q"},
                    {"K", "Q", "K", "Q", "K"},
                    {"A", "W", "A", "A", "Q"}
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line3Win = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 3)
                    .findFirst()
                    .orElse(null);

            assertNotNull(line3Win, "Line 3 should be a winning line");
            assertEquals("A", line3Win.getWinningSymbol(),
                    "Wild should substitute for A, so winning symbol is A");
            assertEquals(0, new BigDecimal("5").compareTo(line3Win.getPayout()),
                    "4-of-a-kind A payout should be 5");
        }

        @Test
        @DisplayName("Scenario C - Mixed Wilds: Line 4 (V-Shape) = [W,W,W,W,W] → A 5-of-a-kind, payout = 10")
        void scenarioC_allWilds_treatedAsA_fiveOfAKind() {
            String[][] grid = {
                    {"W", "Q", "K", "Q", "W"},
                    {"K", "W", "Q", "W", "K"},
                    {"Q", "K", "W", "K", "Q"}
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line4Win = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 4)
                    .findFirst()
                    .orElse(null);

            assertNotNull(line4Win, "Line 4 (V-Shape) should be a winning line");
            assertEquals("A", line4Win.getWinningSymbol(),
                    "All-Wild line should be evaluated as highest paying symbol: A");
            assertEquals(0, new BigDecimal("10").compareTo(line4Win.getPayout()),
                    "5-of-a-kind A payout should be 10");
        }
    }

    @Nested
    @DisplayName("Payout Calculation Tests")
    class PayoutCalculationTests {

        @Test
        @DisplayName("5-of-a-kind A on Line 1 → payout = 10")
        void fiveOfAKindA_payout10() {
            String[][] grid = {
                    {"A", "A", "A", "A", "A"},
                    {"Q", "K", "Q", "K", "Q"},
                    {"K", "Q", "K", "Q", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("105"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("A", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("10").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("4-of-a-kind A on Line 2 → payout = 5")
        void fourOfAKindA_payout5() {
            String[][] grid = {
                    {"Q", "K", "Q", "K", "Q"},
                    {"A", "A", "A", "A", "K"},
                    {"K", "Q", "K", "Q", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("100"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line2 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 2).findFirst().orElse(null);
            assertNotNull(line2);
            assertEquals("A", line2.getWinningSymbol());
            assertEquals(0, new BigDecimal("5").compareTo(line2.getPayout()));
        }

        @Test
        @DisplayName("3-of-a-kind A → payout = 2")
        void threeOfAKindA_payout2() {
            String[][] grid = {
                    {"A", "A", "A", "K", "Q"},
                    {"Q", "K", "Q", "K", "Q"},
                    {"K", "Q", "K", "Q", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("97"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("A", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("2").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("5-of-a-kind K → payout = 5")
        void fiveOfAKindK_payout5() {
            String[][] grid = {
                    {"K", "K", "K", "K", "K"},
                    {"Q", "A", "Q", "A", "Q"},
                    {"A", "Q", "A", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("100"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("K", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("5").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("4-of-a-kind K → payout = 3")
        void fourOfAKindK_payout3() {
            String[][] grid = {
                    {"K", "K", "K", "K", "Q"},
                    {"Q", "A", "Q", "A", "Q"},
                    {"A", "Q", "A", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("98"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("K", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("3").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("5-of-a-kind Q → payout = 2")
        void fiveOfAKindQ_payout2() {
            String[][] grid = {
                    {"Q", "Q", "Q", "Q", "Q"},
                    {"A", "K", "A", "K", "A"},
                    {"K", "A", "K", "A", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("97"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("Q", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("2").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("4-of-a-kind Q → payout = 1")
        void fourOfAKindQ_payout1() {
            String[][] grid = {
                    {"Q", "Q", "Q", "Q", "A"},
                    {"A", "K", "A", "K", "A"},
                    {"K", "A", "K", "A", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("96"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("Q", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("1").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("3-of-a-kind Q → payout = 0.5")
        void threeOfAKindQ_payout05() {
            String[][] grid = {
                    {"Q", "Q", "Q", "A", "K"},
                    {"A", "K", "A", "K", "A"},
                    {"K", "A", "K", "A", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95.5"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("Q", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("0.5").compareTo(line1.getPayout()));
        }
    }

    @Nested
    @DisplayName("No Win Scenarios")
    class NoWinScenarios {

        @Test
        @DisplayName("No matching symbols on any payline → no wins")
        void noWins_differentSymbolsOnEveryLine() {
            String[][] grid = {
                    {"A", "K", "A", "K", "Q"},
                    {"K", "Q", "K", "A", "K"},
                    {"Q", "A", "Q", "K", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            assertTrue(response.getWinningLines().isEmpty(), "No payline should win");
            assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalWin()));
        }

        @Test
        @DisplayName("Only 2 consecutive matching symbols → no win (need at least 3)")
        void twoConsecutiveSymbols_noWin() {
            String[][] grid = {
                    {"A", "A", "K", "Q", "K"},
                    {"K", "K", "Q", "A", "Q"},
                    {"Q", "Q", "A", "K", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            assertTrue(response.getWinningLines().isEmpty(),
                    "2 consecutive symbols should not win");
        }

        @Test
        @DisplayName("Matching symbols not starting from reel 1 → no win")
        void matchingSymbols_notFromReel1_noWin() {
            String[][] grid = {
                    {"A", "K", "K", "K", "K"},
                    {"Q", "A", "Q", "A", "Q"},
                    {"K", "Q", "A", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNull(line1, "Matching symbols not starting from reel 1 should not win");
        }
    }

    @Nested
    @DisplayName("Wild Symbol Tests")
    class WildSymbolTests {

        @Test
        @DisplayName("Wild at start followed by non-wilds: [W, A, A, A, K] → 4-of-a-kind A, payout = 5")
        void wildAtStart_matchesFirstNonWild() {
            String[][] grid = {
                    {"W", "A", "A", "A", "K"},
                    {"Q", "K", "Q", "K", "Q"},
                    {"K", "Q", "K", "Q", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("100"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1, "W followed by 3 A's should win as 4-of-a-kind A");
            assertEquals("A", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("5").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("Two wilds at start: [W, W, K, K, Q] → 4-of-a-kind K, payout = 3")
        void twoWildsAtStart_matchK() {
            String[][] grid = {
                    {"W", "W", "K", "K", "Q"},
                    {"Q", "A", "Q", "A", "Q"},
                    {"K", "Q", "A", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("98"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("K", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("3").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("Wild between symbols: [K, W, K, Q, A] → 3-of-a-kind K, payout = 1")
        void wildBetweenSymbols_substitutes() {
            String[][] grid = {
                    {"K", "W", "K", "Q", "A"},
                    {"Q", "A", "Q", "A", "Q"},
                    {"A", "Q", "A", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("96"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("K", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("1").compareTo(line1.getPayout()));
        }

        @Test
        @DisplayName("Wild does NOT substitute across different symbols: [A, W, K, Q, A] → only 2 matches (A+W), no win")
        void wildDoesNotBridgeDifferentSymbols() {
            String[][] grid = {
                    {"A", "W", "K", "Q", "A"},
                    {"Q", "K", "Q", "A", "Q"},
                    {"K", "Q", "A", "K", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNull(line1, "A, W, K should break at K (only 2 consecutive), no win");
        }
    }

    @Nested
    @DisplayName("Multiple Payline Wins")
    class MultiplePaylineWins {

        @Test
        @DisplayName("Multiple lines winning simultaneously → total payout accumulated")
        void multiplePaylineWins_accumulatedTotalWin() {
            String[][] grid = {
                    {"A", "A", "A", "A", "A"},
                    {"K", "K", "K", "K", "K"},
                    {"Q", "Q", "Q", "Q", "Q"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("112"));

            SpinResponse response = spinService.playSpin(1L, 5);

            assertEquals(3, response.getWinningLines().stream()
                            .filter(w -> w.getLineId() <= 3).count(),
                    "Lines 1, 2, and 3 should all win");

            assertEquals(0, new BigDecimal("17").compareTo(response.getTotalWin()),
                    "Total win should be 10 + 5 + 2 = 17");
        }

        @Test
        @DisplayName("Only highest win per payline (5-of-a-kind, not 3-of-a-kind)")
        void highestWinPerPayline_fiveNotThree() {
            String[][] grid = {
                    {"K", "K", "K", "K", "K"},
                    {"A", "Q", "A", "Q", "A"},
                    {"Q", "A", "Q", "A", "Q"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("100"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line1 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1).findFirst().orElse(null);
            assertNotNull(line1);
            assertEquals("K", line1.getWinningSymbol());
            assertEquals(0, new BigDecimal("5").compareTo(line1.getPayout()),
                    "Should pay 5-of-a-kind multiplier, not 3-of-a-kind");
        }
    }

    @Nested
    @DisplayName("V-Shape and Inverted V-Shape Paylines")
    class DiagonalPaylineTests {

        @Test
        @DisplayName("Line 4 V-Shape win: [0,0],[1,1],[2,2],[1,3],[0,4] all K → 5-of-a-kind K, payout = 5")
        void vShapeWin_fiveOfAKindK() {
            String[][] grid = {
                    {"K", "Q", "A", "Q", "K"},
                    {"Q", "K", "A", "K", "Q"},
                    {"A", "Q", "K", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("100"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line4 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 4).findFirst().orElse(null);
            assertNotNull(line4, "V-Shape line should win");
            assertEquals("K", line4.getWinningSymbol());
            assertEquals(0, new BigDecimal("5").compareTo(line4.getPayout()));
        }

        @Test
        @DisplayName("Line 5 Inverted V-Shape win: [2,0],[1,1],[0,2],[1,3],[2,4] all Q → 5-of-a-kind Q, payout = 2")
        void invertedVShapeWin_fiveOfAKindQ() {
            String[][] grid = {
                    {"K", "A", "Q", "A", "K"},
                    {"A", "Q", "K", "Q", "A"},
                    {"Q", "A", "K", "A", "Q"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("97"));

            SpinResponse response = spinService.playSpin(1L, 5);

            WinningLine line5 = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 5).findFirst().orElse(null);
            assertNotNull(line5, "Inverted V-Shape line should win");
            assertEquals("Q", line5.getWinningSymbol());
            assertEquals(0, new BigDecimal("2").compareTo(line5.getPayout()));
        }
    }

    @Nested
    @DisplayName("Response Integrity Tests")
    class ResponseIntegrityTests {

        @Test
        @DisplayName("Matrix dimensions should be 3 rows x 5 columns")
        void matrixDimensions_3x5() {
            String[][] grid = {
                    {"A", "K", "Q", "A", "K"},
                    {"K", "Q", "A", "K", "Q"},
                    {"Q", "A", "K", "Q", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            assertEquals(3, response.getMatrix().size(), "Matrix should have 3 rows");
            for (List<String> row : response.getMatrix()) {
                assertEquals(5, row.size(), "Each row should have 5 columns");
            }
        }

        @Test
        @DisplayName("Response contains valid newBalance from PlayerService")
        void responseContainsNewBalance() {
            String[][] grid = {
                    {"A", "K", "A", "K", "Q"},
                    {"K", "Q", "K", "A", "K"},
                    {"Q", "A", "Q", "K", "A"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), eq(new BigDecimal("5")), eq(BigDecimal.ZERO)))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = spinService.playSpin(1L, 5);

            assertEquals(0, new BigDecimal("95").compareTo(response.getNewBalance()));
        }

        @Test
        @DisplayName("PlayerService.updateBalanceForSpin is called with correct bet and total win")
        void playerServiceCalledWithCorrectArgs() {
            String[][] grid = {
                    {"A", "A", "A", "A", "A"},
                    {"Q", "K", "Q", "K", "Q"},
                    {"K", "Q", "K", "Q", "K"}
            };
            List<List<String>> matrix = buildMatrix(grid);
            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(anyLong(), any(), any()))
                    .thenReturn(new BigDecimal("105"));

            spinService.playSpin(1L, 5);

            verify(playerService).updateBalanceForSpin(
                    eq(1L),
                    eq(new BigDecimal("5")),
                    argThat(totalWin -> totalWin.compareTo(BigDecimal.ZERO) >= 0)
            );
        }
    }

    @Nested
    @DisplayName("Matrix Generation Tests")
    class MatrixGenerationTests {

        @Test
        @DisplayName("Generated matrix has correct dimensions (3 rows x 5 columns)")
        void generatedMatrix_correctDimensions() {
            SpinService realService = new SpinService(playerService);
            SpinService realSpy = spy(realService);
            when(playerService.updateBalanceForSpin(anyLong(), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = realSpy.playSpin(1L, 5);

            assertEquals(3, response.getMatrix().size(), "Matrix should have 3 rows");
            for (List<String> row : response.getMatrix()) {
                assertEquals(5, row.size(), "Each row should have 5 columns");
            }
        }

        @Test
        @DisplayName("Generated matrix only contains valid symbols (Q, A, K, W)")
        void generatedMatrix_onlyValidSymbols() {
            SpinService realService = new SpinService(playerService);
            when(playerService.updateBalanceForSpin(anyLong(), any(), any()))
                    .thenReturn(new BigDecimal("95"));

            SpinResponse response = realService.playSpin(1L, 5);

            List<String> validSymbols = Arrays.asList("Q", "A", "K", "W");
            for (List<String> row : response.getMatrix()) {
                for (String symbol : row) {
                    assertTrue(validSymbols.contains(symbol),
                            "Symbol '" + symbol + "' is not a valid reel symbol");
                }
            }
        }
    }
}
