package com.example.message_service.dto.request;

import com.example.message_service.model.Visibility;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UpdatePostRequest {
    private String userId;
    private String content;
    private Visibility visibility;
    private List<String> existingImages; // hình cũ giữ lại
}