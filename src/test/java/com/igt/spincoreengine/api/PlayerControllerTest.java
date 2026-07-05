package com.igt.spincoreengine.api;

import com.igt.spincoreengine.api.model.request.DepositRequest;
import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.exception.GlobalApiErrorHandler;
import com.igt.spincoreengine.exception.ServiceException;
import com.igt.spincoreengine.model.WinningLine;
import com.igt.spincoreengine.service.PlayerService;
import com.igt.spincoreengine.service.SpinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PlayerController.class, GlobalApiErrorHandler.class})
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private SpinService spinService;

    @Nested
    @DisplayName("GET /api/v1/players/{id}/balance")
    class GetBalanceEndpoint {

        @Test
        @DisplayName("Returns 200 with balance for existing player")
        void getBalance_existingPlayer_returns200() throws Exception {
            when(playerService.getBalance(1L))
                    .thenReturn(new BalanceResponse(new BigDecimal("100.00")));

            mockMvc.perform(get("/api/v1/players/1/balance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(100.00));
        }

        @Test
        @DisplayName("Returns 404 for non-existing player")
        void getBalance_nonExistingPlayer_returns404() throws Exception {
            when(playerService.getBalance(99L))
                    .thenThrow(new ServiceException("PLayer not found.", HttpStatus.NOT_FOUND));

            mockMvc.perform(get("/api/v1/players/99/balance"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/players/{id}/deposit")
    class DepositEndpoint {

        @Test
        @DisplayName("Deposit with valid amount returns 200")
        void deposit_validAmount_returns200() throws Exception {
            DepositRequest request = new DepositRequest();
            request.setAmount(new BigDecimal("50.00"));

            when(playerService.deposit(eq(1L), any(BigDecimal.class)))
                    .thenReturn(new DepositResponse(new BigDecimal("150.00")));

            mockMvc.perform(post("/api/v1/players/1/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.newBalance").value(150.00));
        }

        @Test
        @DisplayName("Deposit with null amount returns 400")
        void deposit_nullAmount_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": null}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deposit with zero amount returns 400")
        void deposit_zeroAmount_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 0}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deposit with negative amount returns 400")
        void deposit_negativeAmount_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": -10}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deposit to non-existing player returns 404")
        void deposit_nonExistingPlayer_returns404() throws Exception {
            when(playerService.deposit(eq(99L), any(BigDecimal.class)))
                    .thenThrow(new ServiceException("PLayer not found.", HttpStatus.NOT_FOUND));

            mockMvc.perform(post("/api/v1/players/99/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/players/{id}/spin")
    class SpinEndpoint {

        @Test
        @DisplayName("Spin with betAmount=5 returns 200 with matrix and results")
        void spin_validBet_returns200() throws Exception {
            List<List<String>> matrix = Arrays.asList(
                    Arrays.asList("A", "W", "A", "A", "A"),
                    Arrays.asList("Q", "K", "Q", "K", "K"),
                    Arrays.asList("K", "Q", "W", "W", "Q")
            );
            WinningLine winLine = new WinningLine(1, "A", new BigDecimal("10"));
            SpinResponse spinResponse = new SpinResponse(
                    matrix,
                    Collections.singletonList(winLine),
                    new BigDecimal("10"),
                    new BigDecimal("105.00")
            );

            when(spinService.playSpin(eq(1L), eq(5))).thenReturn(spinResponse);

            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 5}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matrix").isArray())
                    .andExpect(jsonPath("$.matrix.length()").value(3))
                    .andExpect(jsonPath("$.winningLines").isArray())
                    .andExpect(jsonPath("$.winningLines[0].lineId").value(1))
                    .andExpect(jsonPath("$.winningLines[0].winningSymbol").value("A"))
                    .andExpect(jsonPath("$.winningLines[0].payout").value(10))
                    .andExpect(jsonPath("$.totalWin").value(10))
                    .andExpect(jsonPath("$.newBalance").value(105.00));
        }

        @Test
        @DisplayName("Spin with betAmount != 5 returns 400 (validation failure)")
        void spin_invalidBet_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 10}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Spin with betAmount = 0 returns 400")
        void spin_zeroBet_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 0}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Spin with null betAmount returns 400")
        void spin_nullBet_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": null}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Spin with betAmount = 3 returns 400")
        void spin_betAmount3_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 3}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Spin with insufficient funds returns 400")
        void spin_insufficientFunds_returns400() throws Exception {
            when(spinService.playSpin(eq(1L), eq(5)))
                    .thenThrow(new ServiceException("Insufficient funds.", HttpStatus.BAD_REQUEST));

            mockMvc.perform(post("/api/v1/players/1/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 5}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Spin for non-existing player returns 404")
        void spin_nonExistingPlayer_returns404() throws Exception {
            when(spinService.playSpin(eq(99L), eq(5)))
                    .thenThrow(new ServiceException("PLayer not found.", HttpStatus.NOT_FOUND));

            mockMvc.perform(post("/api/v1/players/99/spin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"betAmount\": 5}"))
                    .andExpect(status().isNotFound());
        }
    }
}
