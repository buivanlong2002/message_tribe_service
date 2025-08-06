package com.example.message_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "attachments")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    @Id
    private String id;

    @PrePersist
    public void generateId() {
        this.id = UUID.randomUUID().toString();
    }

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    @JsonIgnore
    private Message message;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private long fileSize;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
}
