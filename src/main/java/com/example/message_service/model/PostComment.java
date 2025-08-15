package com.example.message_service.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> replies = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Override
    public String toString() {
        return "PostComment{" +
                "id=" + id +
                ", postId=" + (post != null ? post.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", content='" + content + '\'' +
                ", parentCommentId=" + (parentComment != null ? parentComment.getId() : null) +
                ", repliesCount=" + (replies != null ? replies.size() : 0) +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
