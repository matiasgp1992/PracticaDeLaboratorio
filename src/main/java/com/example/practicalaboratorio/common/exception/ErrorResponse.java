package com.example.practicalaboratorio.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime timestamp
) {
}