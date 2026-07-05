package com.igt.spincoreengine.service;

import com.igt.spincoreengine.api.model.response.BalanceResponse;
import com.igt.spincoreengine.api.model.response.DepositResponse;
import com.igt.spincoreengine.db.entity.Player;
import com.igt.spincoreengine.db.repository.PlayerRepository;
import com.igt.spincoreengine.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setBalance(new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("Get Balance")
    class GetBalanceTests {

        @Test
        @DisplayName("Returns correct balance for existing player")
        void getBalance_existingPlayer_returnsBalance() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

            BalanceResponse response = playerService.getBalance(1L);

            assertNotNull(response);
            assertEquals(0, new BigDecimal("100.00").compareTo(response.getBalance()));
            verify(playerRepository).findById(1L);
        }

        @Test
        @DisplayName("Throws ServiceException for non-existing player")
        void getBalance_nonExistingPlayer_throwsException() {
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ServiceException.class, () -> playerService.getBalance(99L));
        }

        @Test
        @DisplayName("Returns zero balance for player with zero balance")
        void getBalance_zeroBalance() {
            testPlayer.setBalance(BigDecimal.ZERO);
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

            BalanceResponse response = playerService.getBalance(1L);

            assertEquals(0, BigDecimal.ZERO.compareTo(response.getBalance()));
        }
    }

    @Nested
    @DisplayName("Deposit")
    class DepositTests {

        @Test
        @DisplayName("Deposit adds amount to existing balance")
        void deposit_addsToBalance() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            DepositResponse response = playerService.deposit(1L, new BigDecimal("50.00"));

            assertNotNull(response);
            assertEquals(0, new BigDecimal("150.00").compareTo(response.getNewBalance()));
            verify(playerRepository).save(testPlayer);
        }

        @Test
        @DisplayName("Deposit to non-existing player throws ServiceException")
        void deposit_nonExistingPlayer_throwsException() {
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ServiceException.class,
                    () -> playerService.deposit(99L, new BigDecimal("50.00")));
        }

        @Test
        @DisplayName("Deposit small amount (0.01) is handled correctly")
        void deposit_smallAmount() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            DepositResponse response = playerService.deposit(1L, new BigDecimal("0.01"));

            assertEquals(0, new BigDecimal("100.01").compareTo(response.getNewBalance()));
        }

        @Test
        @DisplayName("Deposit large amount is handled correctly")
        void deposit_largeAmount() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            DepositResponse response = playerService.deposit(1L, new BigDecimal("999999.99"));

            assertEquals(0, new BigDecimal("1000099.99").compareTo(response.getNewBalance()));
        }
    }

    @Nested
    @DisplayName("Update Balance For Spin")
    class UpdateBalanceForSpinTests {

        @Test
        @DisplayName("Spin with sufficient funds deducts bet and adds win")
        void spin_sufficientFunds_updatesBalance() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            BigDecimal newBalance = playerService.updateBalanceForSpin(
                    1L, new BigDecimal("5"), new BigDecimal("10"));

            assertEquals(0, new BigDecimal("105.00").compareTo(newBalance));
            verify(playerRepository).save(testPlayer);
        }

        @Test
        @DisplayName("Spin with no win deducts only the bet")
        void spin_noWin_deductsOnlyBet() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            BigDecimal newBalance = playerService.updateBalanceForSpin(
                    1L, new BigDecimal("5"), BigDecimal.ZERO);

            assertEquals(0, new BigDecimal("95.00").compareTo(newBalance));
        }

        @Test
        @DisplayName("Spin with insufficient funds throws ServiceException")
        void spin_insufficientFunds_throwsException() {
            testPlayer.setBalance(new BigDecimal("3.00"));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

            assertThrows(ServiceException.class,
                    () -> playerService.updateBalanceForSpin(
                            1L, new BigDecimal("5"), BigDecimal.ZERO));

            verify(playerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Spin with exact balance equal to bet succeeds")
        void spin_exactBalance_succeeds() {
            testPlayer.setBalance(new BigDecimal("5.00"));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            BigDecimal newBalance = playerService.updateBalanceForSpin(
                    1L, new BigDecimal("5"), BigDecimal.ZERO);

            assertEquals(0, BigDecimal.ZERO.compareTo(newBalance));
        }

        @Test
        @DisplayName("Spin for non-existing player throws ServiceException")
        void spin_nonExistingPlayer_throwsException() {
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ServiceException.class,
                    () -> playerService.updateBalanceForSpin(
                            99L, new BigDecimal("5"), BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Big win correctly updates balance")
        void spin_bigWin_updatesBalance() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

            BigDecimal newBalance = playerService.updateBalanceForSpin(
                    1L, new BigDecimal("5"), new BigDecimal("17"));

            assertEquals(0, new BigDecimal("112.00").compareTo(newBalance));
        }
    }
}
