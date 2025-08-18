package com.example.message_service.repository;

import com.example.message_service.model.NeoPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeoPostRepository extends JpaRepository<NeoPost, Long> {
}
