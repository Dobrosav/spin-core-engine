package com.igt.spincoreengine.db.repository;

import com.igt.spincoreengine.db.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
