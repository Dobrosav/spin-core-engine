package com.igt.spincoreengine.api;

import com.igt.spincoreengine.api.model.request.DepositRequest;
import com.igt.spincoreengine.api.model.request.SpinRequest;
import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.service.PlayerService;
import com.igt.spincoreengine.service.SpinService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;
    private final SpinService spinService;

    public PlayerController(PlayerService playerService, SpinService spinService) {
        this.playerService = playerService;
        this.spinService = spinService;
    }


    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("id") Long playerId) {
        logger.info("Received request to get balance for playerId: {}", playerId);
        BalanceResponse response = playerService.getBalance(playerId);
        logger.info("Successfully fetched balance for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable("id") Long playerId,
            @Valid @RequestBody DepositRequest request) {

        logger.info("Received deposit request for playerId: {} with amount: {}", playerId, request.getAmount());
        DepositResponse response = playerService.deposit(playerId, request.getAmount());
        logger.info("Successfully processed deposit for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/spin")
    public ResponseEntity<SpinResponse> playSpin(
            @PathVariable("id") Long playerId,
            @Valid @RequestBody SpinRequest request) {

        logger.info("Received spin request for playerId: {} with bet amount: {}", playerId, request.getBetAmount());
        SpinResponse response = spinService.playSpin(playerId, request.getBetAmount());
        logger.info("Successfully processed spin for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }
}
