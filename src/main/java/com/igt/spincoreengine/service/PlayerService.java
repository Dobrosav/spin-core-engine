package com.igt.spincoreengine.service;

import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.db.entity.Player;
import com.igt.spincoreengine.db.repository.PlayerRepository;
import com.igt.spincoreengine.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long playerId) {
        logger.info("Fetching balance for playerId: {}", playerId);
        Player player = getPlayerOrThrow(playerId);
        return new BalanceResponse(player.getBalance());
    }

    @Transactional
    public DepositResponse deposit(Long playerId, BigDecimal amount) {
        logger.info("Processing deposit of {} for playerId: {}", amount, playerId);
        Player player = getPlayerOrThrow(playerId);

        BigDecimal newBalance = player.getBalance().add(amount);
        player.setBalance(newBalance);

        playerRepository.save(player);
        logger.info("Successfully deposited. New balance for playerId: {} is {}", playerId, newBalance);

        return new DepositResponse(newBalance);
    }

    @Transactional
    public BigDecimal updateBalanceForSpin(Long playerId, BigDecimal betAmount, BigDecimal winAmount) {
        logger.info("Updating balance for spin. playerId: {}, betAmount: {}, winAmount: {}", playerId, betAmount, winAmount);
        Player player = getPlayerOrThrow(playerId);

        if (player.getBalance().compareTo(betAmount) < 0) {
            logger.warn("Insufficient funds for playerId: {}. Current balance: {}, betAmount: {}", playerId, player.getBalance(), betAmount);
            throw new ServiceException("Insufficient funds.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal newBalance = player.getBalance().subtract(betAmount).add(winAmount);
        player.setBalance(newBalance);

        playerRepository.save(player);
        logger.info("Successfully updated balance for playerId: {}. New balance: {}", playerId, newBalance);

        return newBalance;
    }

    private Player getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> {
                    throw new ServiceException("PLayer not found.", HttpStatus.NOT_FOUND);
                });
    }
}