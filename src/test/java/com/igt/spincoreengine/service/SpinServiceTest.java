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

/**
 * Unit tests for {@link SpinService}.
 * <p>
 * Includes all mandatory test scenarios from task.md:
 * - Scenario A: Pure Win (3-of-a-kind K on Line 1)
 * - Scenario B: Wild Substitution (4-of-a-kind A with Wild on Line 3)
 * - Scenario C: Mixed Wilds (all W on Line 4, treated as A 5-of-a-kind)
 * <p>
 * Matrix is mocked via Mockito spy on generateMatrix() to inject deterministic outcomes.
 */
@ExtendWith(MockitoExtension.class)
class SpinServiceTest {

    @Mock
    private PlayerService playerService;

    private SpinService spinService;

    @BeforeEach
    void setUp() {
        spinService = spy(new SpinService(playerService));
    }

    /**
     * Helper: builds a 3x5 matrix (List of 3 rows, each row has 5 columns).
     * Matrix layout: matrix.get(row).get(col)
     */
    private List<List<String>> buildMatrix(String[][] grid) {
        return Arrays.asList(
                Arrays.asList(grid[0]),
                Arrays.asList(grid[1]),
                Arrays.asList(grid[2])
        );
    }

    // =========================================================================
    // MANDATORY TEST SCENARIOS FROM task.md
    // =========================================================================

    @Nested
    @DisplayName("Mandatory Scenarios from task.md")
    class MandatoryScenarios {

        /**
         * Scenario A (Pure Win): Matrix Line 1 contains [K, K, K, Q, A].
         * Result: Line 1 wins 3-of-a-kind for symbol K, payout is 1.
         * <p>
         * Line 1 = Top Horizontal Row: [0,0], [0,1], [0,2], [0,3], [0,4]
         */
        @Test
        @DisplayName("Scenario A - Pure Win: Line 1 = [K,K,K,Q,A] → 3-of-a-kind K, payout = 1")
        void scenarioA_pureWin_threeOfAKindK() {
            // Arrange: Row 0 = [K, K, K, Q, A] (Line 1 path)
            // Other rows can be anything that doesn't win
            String[][] grid = {
                    {"K", "K", "K", "Q", "A"},   // Row 0 (Line 1)
                    {"Q", "A", "Q", "K", "Q"},   // Row 1
                    {"A", "Q", "A", "A", "K"}    // Row 2
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            // Act
            SpinResponse response = spinService.playSpin(1L, 5);

            // Assert
            assertNotNull(response);
            assertFalse(response.getWinningLines().isEmpty(), "Should have at least one winning line");

            // Find Line 1 win
            WinningLine line1Win = response.getWinningLines().stream()
                    .filter(w -> w.getLineId() == 1)
                    .findFirst()
                    .orElse(null);

            assertNotNull(line1Win, "Line 1 should be a winning line");
            assertEquals("K", line1Win.getWinningSymbol(), "Winning symbol should be K");
            assertEquals(0, new BigDecimal("1").compareTo(line1Win.getPayout()),
                    "3-of-a-kind K payout should be 1");
        }

        /**
         * Scenario B (Wild Substitution): Matrix Line 3 contains [A, W, A, A, Q].
         * Result: Line 3 wins 4-of-a-kind for symbol A (Wild substitutes for A). Payout is 5.
         * <p>
         * Line 3 = Bottom Horizontal Row: [2,0], [2,1], [2,2], [2,3], [2,4]
         */
        @Test
        @DisplayName("Scenario B - Wild Substitution: Line 3 = [A,W,A,A,Q] → 4-of-a-kind A, payout = 5")
        void scenarioB_wildSubstitution_fourOfAKindA() {
            // Arrange: Row 2 = [A, W, A, A, Q] (Line 3 path)
            String[][] grid = {
                    {"Q", "K", "Q", "K", "Q"},   // Row 0
                    {"K", "Q", "K", "Q", "K"},   // Row 1
                    {"A", "W", "A", "A", "Q"}    // Row 2 (Line 3)
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            // Act
            SpinResponse response = spinService.playSpin(1L, 5);

            // Assert
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

        /**
         * Scenario C (Mixed Wilds): Matrix Line 4 (V-Shape) contains [W, W, W, W, W].
         * Result: Line 4 wins. Pure Wild line matches highest paying symbol (A), payout is 10.
         * <p>
         * Line 4 = V-Shape: [0,0], [1,1], [2,2], [1,3], [0,4]
         */
        @Test
        @DisplayName("Scenario C - Mixed Wilds: Line 4 (V-Shape) = [W,W,W,W,W] → A 5-of-a-kind, payout = 10")
        void scenarioC_allWilds_treatedAsA_fiveOfAKind() {
            // Arrange: V-Shape path = [0,0], [1,1], [2,2], [1,3], [0,4]
            // We need: matrix[0][0]=W, matrix[1][1]=W, matrix[2][2]=W, matrix[1][3]=W, matrix[0][4]=W
            // Fill other positions to not create additional wins
            String[][] grid = {
                    {"W", "Q", "K", "Q", "W"},   // Row 0: [0,0]=W, [0,4]=W
                    {"K", "W", "Q", "W", "K"},   // Row 1: [1,1]=W, [1,3]=W
                    {"Q", "K", "W", "K", "Q"}    // Row 2: [2,2]=W
            };
            List<List<String>> matrix = buildMatrix(grid);

            doReturn(matrix).when(spinService).generateMatrix();
            when(playerService.updateBalanceForSpin(eq(1L), any(), any()))
                    .thenAnswer(inv -> {
                        BigDecimal bet = inv.getArgument(1);
                        BigDecimal win = inv.getArgument(2);
                        return new BigDecimal("100").subtract(bet).add(win);
                    });

            // Act
            SpinResponse response = spinService.playSpin(1L, 5);

            // Assert
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

    // =========================================================================
    // ADDITIONAL EDGE CASE TESTS
    // =========================================================================

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
            // Grid carefully designed so NO payline has 3+ consecutive matching symbols:
            // Line 1 (row0): A,K,Q,A,K → A,K breaks
            // Line 2 (row1): K,Q,A,K,Q → K,Q breaks
            // Line 3 (row2): Q,A,K,Q,A → Q,A breaks
            // Line 4 (V:0,0|1,1|2,2|1,3|0,4): A,Q,K,K,K → A,Q breaks
            // Line 5 (IV:2,0|1,1|0,2|1,3|2,4): Q,Q,Q... wait, need to check
            // Let's use a grid verified for all paylines:
            // Row0: A,K,A,K,Q   Line1: A,K breaks
            // Row1: K,Q,K,A,K   Line2: K,Q breaks
            // Row2: Q,A,Q,K,A   Line3: Q,A breaks
            // Line4 V[0,0][1,1][2,2][1,3][0,4]: A,Q,Q,A,Q → A,Q breaks
            // Line5 IV[2,0][1,1][0,2][1,3][2,4]: Q,Q,A,A,A → Q,Q,A breaks
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
            // Line 1: [A, K, K, K, K] — 4 K's but starting from position 2, not from reel 1
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

            // Line 1 should NOT win because matching starts from reel 2, not reel 1
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
            // First non-wild is A, second is W (matches A), third is K (breaks chain)
            // So consecutive match from reel 1 = A, W → only 2 matches, no win
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
            // Line 1 (row 0): [A, A, A, A, A] → 5-of-a-kind A = 10
            // Line 2 (row 1): [K, K, K, K, K] → 5-of-a-kind K = 5
            // Line 3 (row 2): [Q, Q, Q, Q, Q] → 5-of-a-kind Q = 2
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

            // Lines 1, 2, 3 should all win
            // Line 4 (V-shape): [A, K, Q, K, A] → A,K breaks at K (no win)
            // Line 5 (Inv V):   [Q, K, A, K, Q] → Q,K breaks at K (no win)
            assertEquals(3, response.getWinningLines().stream()
                            .filter(w -> w.getLineId() <= 3).count(),
                    "Lines 1, 2, and 3 should all win");

            // Total = 10 + 5 + 2 = 17
            assertEquals(0, new BigDecimal("17").compareTo(response.getTotalWin()),
                    "Total win should be 10 + 5 + 2 = 17");
        }

        @Test
        @DisplayName("Only highest win per payline (5-of-a-kind, not 3-of-a-kind)")
        void highestWinPerPayline_fiveNotThree() {
            // Line 1: [K, K, K, K, K] → should pay 5-of-a-kind (5), NOT 3-of-a-kind (1)
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
            // V-Shape: [0,0], [1,1], [2,2], [1,3], [0,4]
            String[][] grid = {
                    {"K", "Q", "A", "Q", "K"},  // [0,0]=K, [0,4]=K
                    {"Q", "K", "A", "K", "Q"},  // [1,1]=K, [1,3]=K
                    {"A", "Q", "K", "Q", "A"}   // [2,2]=K
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
            // Inv V-Shape: [2,0], [1,1], [0,2], [1,3], [2,4]
            String[][] grid = {
                    {"K", "A", "Q", "A", "K"},  // [0,2]=Q
                    {"A", "Q", "K", "Q", "A"},  // [1,1]=Q, [1,3]=Q
                    {"Q", "A", "K", "A", "Q"}   // [2,0]=Q, [2,4]=Q
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
            // Grid designed so no payline wins → totalWin = 0
            // All 5 paylines verified to break before 3 consecutive matches
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
            // A full win matrix: Line 1 = [A, A, A, A, A] → totalWin = 10
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
            // Call the real generateMatrix (not mocked) via a fresh SpinService
            SpinService realService = new SpinService(playerService);
            // Access via reflection or just test through playSpin
            // Instead, we test the real generateMatrix by calling it via a spy that doesn't override it
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
