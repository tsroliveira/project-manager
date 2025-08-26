package com.portfolio.web.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Formato padronizado de erro da API")
public record ErrorResponse(
        @Schema(example = "2025-08-25T17:30:27.418820755-03:00")
        OffsetDateTime timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "VALIDATION_ERROR") String code,
        @Schema(example = "Dados inválidos. Corrija os campos indicados.")
        String message,
        @Schema(example = "/api/projects") String path,
        List<Violation> violations
) {
    @Schema(name = "ErrorViolation", description = "Erro de validação de campo")
    public record Violation(
            @Schema(example = "name") String field,
            @Schema(example = "must not be blank") String message
    ) {}
}
