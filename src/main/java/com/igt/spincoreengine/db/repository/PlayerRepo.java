package com.igt.spincoreengine.db.repository;

import com.igt.spincoreengine.db.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepo extends JpaRepository<Player, Long> {
}
