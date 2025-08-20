package com.example.message_service.repository;

import com.example.message_service.model.CallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallHistoryRepository extends JpaRepository<CallHistory, String> {
    
    // Lấy lịch sử cuộc gọi theo conversation
    @Query("SELECT ch FROM CallHistory ch WHERE ch.conversationId = :conversationId ORDER BY ch.startTime DESC")
    List<CallHistory> findByConversationIdOrderByStartTimeDesc(@Param("conversationId") String conversationId);
    
    // Lấy lịch sử cuộc gọi theo user
    @Query("SELECT ch FROM CallHistory ch WHERE ch.callerId = :userId OR ch.receiverId = :userId ORDER BY ch.startTime DESC")
    List<CallHistory> findByUserIdOrderByStartTimeDesc(@Param("userId") String userId);
    
    // Lấy cuộc gọi nhỡ theo user
    @Query("SELECT ch FROM CallHistory ch WHERE (ch.callerId = :userId OR ch.receiverId = :userId) AND ch.callStatus IN ('MISSED', 'TIMEOUT', 'REJECTED') ORDER BY ch.startTime DESC")
    List<CallHistory> findMissedCallsByUserId(@Param("userId") String userId);
    
    // Lấy cuộc gọi theo session ID
    CallHistory findBySessionId(String sessionId);
    
    // Đếm cuộc gọi nhỡ theo user
    @Query("SELECT COUNT(ch) FROM CallHistory ch WHERE (ch.callerId = :userId OR ch.receiverId = :userId) AND ch.callStatus IN ('MISSED', 'TIMEOUT', 'REJECTED')")
    long countMissedCallsByUserId(@Param("userId") String userId);
}
