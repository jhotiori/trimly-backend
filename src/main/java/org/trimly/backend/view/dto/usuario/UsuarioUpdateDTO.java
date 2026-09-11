package org.trimly.backend.view.dto.usuario;

import jakarta.validation.constraints.Email;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;

/**
 * Campos atualizáveis de um usuário. Todos são opcionais (semântica de PATCH); uma requisição com
 * todos nulos é um no-op.
 *
 * @param nome - novo nome
 * @param email - novo e-mail
 * @param senha - nova senha em texto puro, recriptografada quando enviada
 * @param cargo - novo cargo
 */
public record UsuarioUpdateDTO(
        String nome,

        @Email(message = "Não foi recebido um formato de e-mail válido")
        String email,

        String senha,
        UsuarioCargo cargo) {}
