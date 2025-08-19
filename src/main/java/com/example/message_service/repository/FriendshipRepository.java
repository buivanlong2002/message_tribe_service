package com.example.message_service.repository;

import com.example.message_service.model.Friendship;
import com.example.message_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

    // Tìm tất cả friendship mà user tham gia vào (có thể là sender hoặc receiver)
    List<Friendship> findBySenderOrReceiver(User user1, User user2);

    // Tìm tất cả friendship mà user tham gia vào (có thể là sender hoặc receiver)
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user OR f.receiver = :user)")
    List<Friendship> findFriendshipsByUser(@Param("user") User user);

    List<Friendship> findByStatusAndReceiver(String status, User receiver);

    List<Friendship> findByStatusAndSender(String status, User sender);

    boolean existsBySenderAndReceiver(User sender, User receiver);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, String status);

    List<Friendship> findBySenderAndStatus(User sender, String status);

    List<Friendship> findByReceiverAndStatus(User receiver, String status);
}
