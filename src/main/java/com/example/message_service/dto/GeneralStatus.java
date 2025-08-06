package com.example.message_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "General status information for API responses")
public class GeneralStatus implements Serializable {

    @Schema(description = "Status code", example = "00")
    private String code;

    @Schema(description = "Indicates success or failure", example = "true")
    private boolean success;

    @JsonProperty("message")
    @Schema(description = "Message associated with the status code", example = "Success")
    private String message;

    @JsonProperty("responseTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Response time in ISO 8601 format", example = "2023-10-01T12:00:00Z")
    private Date responseTime = new Date();

    @JsonProperty("displayMessage")
    @Schema(description = "Message to be displayed to the user", example = "Operation completed successfully")
    private String displayMessage;

    public GeneralStatus(String code, boolean success) {
        this.code = code;
        this.success = success;
        this.message = success ? "Success" : "Unknown Error";
        this.displayMessage = this.message;
        this.responseTime = new Date();
    }


}
