package org.trimly.backend.view.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para a criação de um usuário.
 *
 * @param nome - nome do usuário
 * @param email - e-mail do usuário
 * @param senha - senha em texto puro, criptografada antes de persistir
 */
public record UsuarioCreateDTO(
        @Schema(description = "Nome completo do usuário", example = "Ana Souza") @NotBlank(
                message = "Nome não pode ser vazio"
        ) String nome,

        @Schema(description = "E-mail de acesso, único entre os usuários", example = "ana.souza@trimly.com") @NotBlank(
                message = "Email não pode ser vazio"
        ) @Email(message = "Não foi recebido um formato de e-mail válido") String email,

        @Schema(
                description = "Senha em texto puro; é armazenada criptografada com BCrypt",
                example = "123456"
        ) @NotBlank(
                message = "Senha não pode ser vazia"
        ) @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senha
) {}
