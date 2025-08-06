package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_media")
@Data
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
}

