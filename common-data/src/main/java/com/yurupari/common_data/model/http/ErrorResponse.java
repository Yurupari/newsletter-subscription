package com.yurupari.common_data.model.http;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Builder
public record ErrorResponse(
        HttpStatus httpStatus,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") OffsetDateTime timestamp,
        String message
) {
}
