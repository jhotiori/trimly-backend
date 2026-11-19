package org.trimly.backend.view.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;

/**
 * Resposta de autenticação, contendo o token JWT emitido e os dados públicos do usuário.
 *
 * @param token - token JWT assinado
 * @param usuario - dados públicos do usuário autenticado
 */
public record AuthResponseDTO(
        @Schema(
                description = "Token JWT assinado",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmltbHktYXV0aC1hcGkiLCJzdWIiOiJhZG1pbkB0cmltbHkuY29tIn0.c2lnbmF0dXJl"
        ) String token, @Schema(description = "Dados públicos do usuário autenticado") UsuarioResponseDTO usuario
) {}
