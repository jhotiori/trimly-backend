package org.trimly.backend.view.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de autenticação, contendo o token JWT emitido.
 *
 * @param token - token JWT assinado
 */
public record AuthResponseDTO(
        @Schema(
                description = "Token JWT assinado com HMAC256, com o e-mail como subject e o cargo como claim",
                example =
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmltbHktYXV0aC1hcGkiLCJzdWIiOiJhZG1pbkB0cmltbHkuY29tIn0.c2lnbmF0dXJl")
        String token) {}
