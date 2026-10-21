package org.trimly.backend.view.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;

/**
 * Resposta de um usuário, sem a senha.
 */
public record UsuarioResponseDTO(
        @Schema(description = "Identificador do usuário", example = "1") Long id,

        @Schema(description = "Nome completo do usuário", example = "Administrador") String nome,

        @Schema(description = "E-mail de acesso, único entre os usuários", example = "admin@trimly.com") String email,

        @Schema(description = "Cargo que define o nível de acesso", example = "ADMIN") UsuarioCargo cargo
) {}
