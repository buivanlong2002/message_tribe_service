package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "neo_posts")
public class NeoPost {

    @Id
    private String id;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NeoPostVisibility visibility = NeoPostVisibility.PUBLIC;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "neoPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NeoPostReaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "neoPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NeoPostComment> comments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "neo_post_media_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "url")
    private List<String> urlMedia = new ArrayList<>();
}