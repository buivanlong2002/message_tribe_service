package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String mediaUrl;

    private String mediaType;

    public PostMedia(Post savedPost, String url, String contentType) {
        this.post = savedPost;
        this.mediaUrl = url;
        this.mediaType = contentType;
    }

    @Override
    public String toString() {
        return "PostMedia{" +
                "id=" + id +
                ", postId=" + (post != null ? post.getId() : null) +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                '}';
    }
}

