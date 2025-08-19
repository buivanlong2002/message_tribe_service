package com.example.message_service.dto.request;

import com.example.message_service.model.NeoPostReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReactionRequest {
    private NeoPostReactionType type;
}
