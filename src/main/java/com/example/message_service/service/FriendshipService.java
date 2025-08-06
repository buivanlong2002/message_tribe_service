package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.BlockedUserResponse;
import com.example.message_service.dto.response.FriendResponse;
import com.example.message_service.dto.response.PendingFriendRequestResponse;
import com.example.message_service.model.Friendship;
import com.example.message_service.model.User;
import com.example.message_service.repository.FriendshipRepository;
import com.example.message_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<String> sendFriendRequest(String senderId, String receiverId) {
        // Tìm sender và receiver
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ApiResponse.error("01", "Người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        // Kiểm tra xem đã có mối quan hệ kết bạn chưa (cả 2 chiều)
        boolean exists = friendshipRepository.existsBySenderAndReceiver(sender, receiver)
                || friendshipRepository.existsBySenderAndReceiver(receiver, sender);
        if (exists) {
            return ApiResponse.error("02", "Mối quan hệ kết bạn đã tồn tại");
        }

        // Tạo và lưu mối quan hệ kết bạn mới
        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus("pending");
        friendshipRepository.save(friendship);

        return ApiResponse.success("00","Lời mời kết bạn đã được gửi");
    }


    public ApiResponse<String> acceptFriendRequest(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ApiResponse.error("02", "Người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        Optional<Friendship> friendshipOpt = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        if (friendshipOpt.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy lời mời kết bạn");
        }

        Friendship friendship = friendshipOpt.get();
        if (!"pending".equals(friendship.getStatus())) {
            return ApiResponse.error("04", "Lời mời kết bạn không ở trạng thái chờ");
        }

        friendship.setStatus("accepted");
        friendship.setAcceptedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);

        return ApiResponse.success("00", "Lời mời kết bạn đã được chấp nhận", null);
    }


    // Từ chối lời mời kết bạn
    public ApiResponse<String> rejectFriendRequest(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ApiResponse.error("02", "Người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        Optional<Friendship> friendshipOpt = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        if (friendshipOpt.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy lời mời kết bạn để từ chối");
        }

        friendshipRepository.delete(friendshipOpt.get());
        return ApiResponse.success("00", "Lời mời kết bạn đã bị từ chối", null);
    }

    public ApiResponse<List<FriendResponse>> getFriendships(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("02", "Người dùng không tồn tại");
        }

        User user = userOpt.get();
        List<Friendship> friendships = friendshipRepository.findBySenderOrReceiver(user, user);

        List<FriendResponse> friends = friendships.stream()
                .filter(f -> "accepted".equals(f.getStatus()))
                .map(f -> {
                    User friend = f.getSender().equals(user) ? f.getReceiver() : f.getSender();
                    return new FriendResponse(friend.getId(), friend.getDisplayName(), friend.getAvatarUrl());
                })
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách bạn bè thành công", friends);
    }




    // Lấy tất cả lời mời kết bạn đang chờ chấp nhận (status = "pending") cho một người nhận
    public ApiResponse<List<PendingFriendRequestResponse>> getPendingRequests(String userId) {
        Optional<User> receiverOpt = userRepository.findById(userId);
        if (receiverOpt.isEmpty()) {
            return ApiResponse.error("02", "Người nhận không tồn tại");
        }

        User receiver = receiverOpt.get();

        // Kiểm tra xem receiver ID có đúng như bạn mong muốn
        System.out.println("Tìm lời mời kết bạn cho receiverId: " + receiver.getId());

        List<Friendship> pendingRequests = friendshipRepository.findByStatusAndReceiver("pending", receiver);

        System.out.println("Tổng số lời mời pending tìm được: " + pendingRequests.size());

        List<PendingFriendRequestResponse> dtoList = pendingRequests.stream()
                .map(f -> {
                    User sender = f.getSender();

                    return new PendingFriendRequestResponse(
                            sender.getId(),
                            sender.getDisplayName(),
                            sender.getAvatarUrl(),
                            receiver.getId(),
                            receiver.getDisplayName(),
                            receiver.getAvatarUrl(),
                            f.getRequestedAt()
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách lời mời kết bạn đang chờ thành công", dtoList);
    }

    public ApiResponse<List<BlockedUserResponse>> getBlockedUsers(String userId) {
        Optional<User> senderOpt = userRepository.findById(userId);
        if (senderOpt.isEmpty()) {
            return ApiResponse.error("02", "Người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        List<Friendship> blockedFriendships = friendshipRepository.findBySenderAndStatus(sender, "blocked");

        List<BlockedUserResponse> blockedUsers = blockedFriendships.stream()
                .map(f -> {
                    User blocked = f.getReceiver();
                    return new BlockedUserResponse(
                            blocked.getId(),
                            blocked.getDisplayName(),
                            blocked.getAvatarUrl()
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách người bị chặn thành công", blockedUsers);
    }


    public ApiResponse<String> unblockUser(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ApiResponse.error("01", "Người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        Optional<Friendship> friendshipOpt = friendshipRepository.findBySenderAndReceiver(sender, receiver);

        if (friendshipOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy mối quan hệ giữa hai người dùng");
        }

        Friendship friendship = friendshipOpt.get();
        if (!"blocked".equals(friendship.getStatus())) {
            return ApiResponse.error("03", "Người này không nằm trong danh sách bị chặn");
        }

        friendshipRepository.delete(friendship);
        return ApiResponse.success("00", "Bỏ chặn người dùng thành công", null);
    }

    public ApiResponse<String> unfriend(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ApiResponse.error("01", "Một hoặc cả hai người dùng không tồn tại");
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        // Tìm mối quan hệ kết bạn từ cả hai chiều
        Optional<Friendship> friendshipOpt1 = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friendship> friendshipOpt2 = friendshipRepository.findBySenderAndReceiver(receiver, sender);

        if (friendshipOpt1.isPresent() && "accepted".equals(friendshipOpt1.get().getStatus())) {
            friendshipRepository.delete(friendshipOpt1.get());
            return ApiResponse.success("00", "Đã hủy kết bạn thành công", null);
        }

        if (friendshipOpt2.isPresent() && "accepted".equals(friendshipOpt2.get().getStatus())) {
            friendshipRepository.delete(friendshipOpt2.get());
            return ApiResponse.success("00", "Đã hủy kết bạn thành công", null);
        }

        return ApiResponse.error("02", "Hai người không phải bạn bè");
    }

    // ✅ Lấy danh sách lời mời kết bạn đã gửi (pending)
    public ApiResponse<List<PendingFriendRequestResponse>> getSentPendingRequests(String senderId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        if (senderOpt.isEmpty()) {
            return ApiResponse.error("02", "Người gửi không tồn tại");
        }

        User sender = senderOpt.get();

        List<Friendship> pendingRequests = friendshipRepository.findByStatusAndSender("pending", sender);

        List<PendingFriendRequestResponse> dtoList = pendingRequests.stream()
                .map(f -> {
                    User receiver = f.getReceiver();
                    return new PendingFriendRequestResponse(
                            sender.getId(),
                            sender.getDisplayName(),
                            sender.getAvatarUrl(),
                            receiver.getId(),                  // ✅ truyền thêm receiverId
                            receiver.getDisplayName(),
                            receiver.getAvatarUrl(),
                            f.getRequestedAt()
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách lời mời kết bạn đã gửi thành công", dtoList);
    }

}
