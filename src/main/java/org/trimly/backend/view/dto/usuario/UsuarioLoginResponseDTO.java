package org.trimly.backend.view.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado de um login por credenciais. O campo {@code usuario} é nulo quando {@code sucesso} é
 * {@code false}.
 *
 * @param sucesso - indica se as credenciais conferem
 * @param usuario - usuário autenticado, ou nulo quando o login falha
 */
public record UsuarioLoginResponseDTO(
        @Schema(description = "Indica se o e-mail e a senha conferem", example = "true")
        boolean sucesso,

        @Schema(
                description = "Usuário autenticado; nulo quando sucesso é false",
                example =
                        "{\"id\": 1, \"nome\": \"Administrador\", \"email\": \"admin@trimly.com\", \"cargo\": \"ADMIN\"}")
        UsuarioResponseDTO usuario) {}
