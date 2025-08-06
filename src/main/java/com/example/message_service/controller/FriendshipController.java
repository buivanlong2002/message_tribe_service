package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.BlockedUserResponse;
import com.example.message_service.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.message_service.dto.response.FriendResponse;
import com.example.message_service.dto.response.PendingFriendRequestResponse;


import java.util.List;

@RestController
@RequestMapping("api/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    // Gửi lời mời kết bạn
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        ApiResponse<String> response = friendshipService.sendFriendRequest(senderId, receiverId);

        if (response.getStatus().isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    // Chấp nhận lời mời kết bạn
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        ApiResponse<String> response = friendshipService.acceptFriendRequest(senderId, receiverId);
        if (response.getStatus().isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    // Từ chối lời mời kết bạn
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        ApiResponse<String> response = friendshipService.rejectFriendRequest(senderId, receiverId);

        if (response.getStatus().isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> listFriendShips(@RequestParam String userId) {
        ApiResponse<List<FriendResponse>> response = friendshipService.getFriendships(userId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/friend-requests")
    public ResponseEntity<ApiResponse<List<PendingFriendRequestResponse>>> getPendingFriendRequests(@RequestParam String userId) {
        ApiResponse<List<PendingFriendRequestResponse>> response = friendshipService.getPendingRequests(userId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked-users")
    public ResponseEntity<ApiResponse<List<BlockedUserResponse>>> getBlockedUsers(@RequestParam String userId) {
        ApiResponse<List<BlockedUserResponse>> response = friendshipService.getBlockedUsers(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unblock")
    public ResponseEntity<ApiResponse<String>> unblockUser(@RequestParam String senderId,
                                                           @RequestParam String receiverId) {
        ApiResponse<String> response = friendshipService.unblockUser(senderId, receiverId);
        return new ResponseEntity<>(response,
                response.getStatus().isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/unfriend")
    public ResponseEntity<ApiResponse<String>> unfriend(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        ApiResponse<String> response = friendshipService.unfriend(senderId, receiverId);
        return new ResponseEntity<>(response,
                response.getStatus().isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/sent-requests")
    public ResponseEntity<ApiResponse<List<PendingFriendRequestResponse>>> getSentFriendRequests(
            @RequestParam String senderId) {
        ApiResponse<List<PendingFriendRequestResponse>> response = friendshipService.getSentPendingRequests(senderId);
        return ResponseEntity.ok(response);
    }

}
