package com.example.message_service.repository;

import com.example.message_service.model.NeoPostReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeoPostReactionRepository extends JpaRepository<NeoPostReaction, Long> {
}
