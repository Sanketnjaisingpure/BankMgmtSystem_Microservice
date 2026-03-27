package com.bank.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

   /* @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")*/
    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    // For validation errors
    private List<ValidationError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}