package com.example.message_service.dto.request;

import com.example.message_service.model.Visibility;
import lombok.Data;

@Data
public class CreatePostRequest {
    private String userId;
    private String content;
    private Visibility visibility = Visibility.PUBLIC;
}

