package com.igt.spincoreengine.service;

import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.db.entity.Player;
import com.igt.spincoreengine.db.repository.PlayerRepository;
import com.igt.spincoreengine.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long playerId) {
        Player player = getPlayerOrThrow(playerId);
        return new BalanceResponse(player.getBalance());
    }

    @Transactional
    public DepositResponse deposit(Long playerId, BigDecimal amount) {
        Player player = getPlayerOrThrow(playerId);

        BigDecimal newBalance = player.getBalance().add(amount);
        player.setBalance(newBalance);

        playerRepository.save(player);

        return new DepositResponse(newBalance);
    }

    @Transactional
    public BigDecimal updateBalanceForSpin(Long playerId, BigDecimal betAmount, BigDecimal winAmount) {
        Player player = getPlayerOrThrow(playerId);

        if (player.getBalance().compareTo(betAmount) < 0) {
            throw new ServiceException("Insufficient funds.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal newBalance = player.getBalance().subtract(betAmount).add(winAmount);
        player.setBalance(newBalance);

        playerRepository.save(player);

        return newBalance;
    }

    private Player getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> {throw new ServiceException("PLayer not found.", HttpStatus.NOT_FOUND);});
    }
}