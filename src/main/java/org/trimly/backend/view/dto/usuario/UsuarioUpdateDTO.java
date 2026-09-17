package org.trimly.backend.view.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
        @Schema(description = "Opcional. Novo nome completo", example = "Ana Souza Lima") String nome,

        @Schema(description = "Opcional. Novo e-mail, único entre os usuários", example = "ana.lima@trimly.com") @Email(
                message = "Não foi recebido um formato de e-mail válido"
        ) String email,

        @Schema(
                description = "Opcional. Nova senha em texto puro, recriptografada ao salvar",
                example = "novaSenha123"
        ) @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senha,

        @Schema(description = "Opcional. Novo cargo", example = "ADMIN") UsuarioCargo cargo
) {}
