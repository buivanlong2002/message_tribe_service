package com.example.message_service.dto.request;

import com.example.message_service.model.Visibility;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {
    private String content;
    private List<String> mediaUrls;
    private Visibility visibility;
} 