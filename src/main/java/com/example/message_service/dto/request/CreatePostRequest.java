package com.example.message_service.dto.request;

import com.example.message_service.model.NeoPostVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {
    private String content;
    private NeoPostVisibility visibility = NeoPostVisibility.PUBLIC;
    private List<String> mediaUrls;
}
