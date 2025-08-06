package com.example.message_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "General status information")
    private GeneralStatus status;

    @Schema(description = "Payload data returned by the API")
    private T data;

    public static <T> ApiResponse<T> success(String displayMessage, T data) {
        GeneralStatus status = new GeneralStatus("00", true);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, data);
    }

    public static <T> ApiResponse<T> success(String code, String displayMessage, T data) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, data);
    }

    public static <T> ApiResponse<T> error(String code, String displayMessage) {
        GeneralStatus status = new GeneralStatus(code, false);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, null);
    }

    public static <T> ApiResponse<T> error(String code, String displayMessage, T data) {
        GeneralStatus status = new GeneralStatus(code, false);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, data);
    }

}
