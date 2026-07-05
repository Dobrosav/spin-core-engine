package com.igt.spincoreengine.api;

import com.igt.spincoreengine.api.model.request.DepositRequest;
import com.igt.spincoreengine.api.model.request.SpinRequest;
import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.api.model.response.ErrorResponse;
import com.igt.spincoreengine.api.model.response.SpinResponse;
import com.igt.spincoreengine.service.PlayerService;
import com.igt.spincoreengine.service.SpinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Player Management", description = "Endpoints for managing player balance, deposits and spins")
public class PlayerController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;
    private final SpinService spinService;

    public PlayerController(PlayerService playerService, SpinService spinService) {
        this.playerService = playerService;
        this.spinService = spinService;
    }


    @Operation(summary = "Get player balance", description = "Retrieves the current balance of the specified player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved balance",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "ID of the player", required = true) @PathVariable("id") Long playerId) {
        logger.info("Received request to get balance for playerId: {}", playerId);
        BalanceResponse response = playerService.getBalance(playerId);
        logger.info("Successfully fetched balance for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Deposit funds", description = "Deposits the specified amount into the player's account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deposited funds",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid deposit amount",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @Parameter(description = "ID of the player", required = true) @PathVariable("id") Long playerId,
            @Parameter(description = "Deposit request object containing the amount", required = true) @Valid @RequestBody DepositRequest request) {

        logger.info("Received deposit request for playerId: {} with amount: {}", playerId, request.getAmount());
        DepositResponse response = playerService.deposit(playerId, request.getAmount());
        logger.info("Successfully processed deposit for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Play a spin", description = "Executes a spin game for the player with the specified bet amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully executed spin",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpinResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid bet amount or insufficient funds",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/spin")
    public ResponseEntity<SpinResponse> playSpin(
            @Parameter(description = "ID of the player", required = true) @PathVariable("id") Long playerId,
            @Parameter(description = "Spin request object containing the bet amount", required = true) @Valid @RequestBody SpinRequest request) {

        logger.info("Received spin request for playerId: {} with bet amount: {}", playerId, request.getBetAmount());
        SpinResponse response = spinService.playSpin(playerId, request.getBetAmount());
        logger.info("Successfully processed spin for playerId: {}", playerId);
        return ResponseEntity.ok(response);
    }
}
