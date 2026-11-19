package org.trimly.backend.view.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais informadas no login.
 *
 * @param email - e-mail do usuário
 * @param senha - senha em texto puro
 */
public record AuthLoginRequestDTO(
        @Schema(description = "E-mail cadastrado do usuário", example = "admin@trimly.com")
        @NotBlank(message = "E-mail não pode ser vazio")
        @Email(message = "E-mail inválido") String email,

        @Schema(description = "Senha do usuário, em texto puro", example = "admin123")
        @NotBlank(message = "Senha não pode ser vazia") String senha
) {}
