package org.trimly.backend.view.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para a criação de um usuário.
 *
 * @param nome - nome do usuário
 * @param email - e-mail do usuário
 * @param senha - senha em texto puro, criptografada antes de persistir
 */
public record UsuarioCreateDTO(
        @NotBlank(message = "Nome não pode ser vazio") String nome,

        @NotBlank(message = "Email não pode ser vazio") @Email(message = "Não foi recebido um formato de e-mail válido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia") String senha) {}
