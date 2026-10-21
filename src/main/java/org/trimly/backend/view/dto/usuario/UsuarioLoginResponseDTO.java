package org.trimly.backend.view.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado do login; `usuario` é nulo quando `sucesso` é `false`.
 */
public record UsuarioLoginResponseDTO(
        @Schema(description = "Indica se o e-mail e a senha conferem", example = "true") boolean sucesso,

        @Schema(
                description = "Usuário autenticado; nulo quando sucesso é false",
                example = "{\"id\": 1, \"nome\": \"Administrador\", \"email\": \"admin@trimly.com\", \"cargo\": \"ADMIN\"}"
        ) UsuarioResponseDTO usuario
) {}
