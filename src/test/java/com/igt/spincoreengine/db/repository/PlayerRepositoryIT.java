package com.igt.spincoreengine.db.repository;

import com.igt.spincoreengine.AbstractIntegrationTest;
import com.igt.spincoreengine.db.entity.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlayerRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void save_andFindById_returnsPlayer() {
        Player player = new Player();
        player.setBalance(new BigDecimal("100.00"));

        Player saved = playerRepository.save(player);

        Optional<Player> found = playerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("100.00").compareTo(found.get().getBalance()));
    }

    @Test
    void findById_nonExistingPlayer_returnsEmpty() {
        Optional<Player> found = playerRepository.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void save_updatesBalance() {
        Player player = new Player();
        player.setBalance(new BigDecimal("100.00"));
        player = playerRepository.save(player);

        player.setBalance(new BigDecimal("150.00"));
        playerRepository.save(player);

        Player updated = playerRepository.findById(player.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("150.00").compareTo(updated.getBalance()));
    }

    @Test
    void delete_removesPlayer() {
        Player player = new Player();
        player.setBalance(new BigDecimal("50.00"));
        player = playerRepository.save(player);
        Long id = player.getId();

        playerRepository.deleteById(id);

        assertTrue(playerRepository.findById(id).isEmpty());
    }
}
