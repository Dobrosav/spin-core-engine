package com.igt.spincoreengine.api;

import com.igt.spincoreengine.api.model.request.DepositRequest;
import com.igt.spincoreengine.api.model.request.SpinRequest;
import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.service.PlayerService;
import com.igt.spincoreengine.service.SpinService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;
    private final SpinService spinService;

    public PlayerController(PlayerService playerService, SpinService spinService) {
        this.playerService = playerService;
        this.spinService = spinService;
    }


    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("id") Long playerId) {
        BalanceResponse response = playerService.getBalance(playerId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable("id") Long playerId,
            @Valid @RequestBody DepositRequest request) {

        DepositResponse response = playerService.deposit(playerId, request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/spin")
    public ResponseEntity<SpinResponse> playSpin(
            @PathVariable("id") Long playerId,
            @Valid @RequestBody SpinRequest request) {

        SpinResponse response = spinService.playSpin(playerId, request.getBetAmount());
        return ResponseEntity.ok(response);
    }
}
