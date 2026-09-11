package org.trimly.backend.view.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais informadas no login.
 *
 * @param email - e-mail do usuário
 * @param senha - senha em texto puro
 */
public record AuthLoginRequestDTO(
        @NotBlank(message = "Email não pode ser vazio") @Email(message = "Não foi recebido um formato de e-mail válido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia") String senha) {}
