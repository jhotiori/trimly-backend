package org.trimly.backend.view.dto.usuario;

import org.trimly.backend.model.entity.usuario.UsuarioCargo;

/**
 * Representação de resposta de um usuário. Não expõe a senha.
 *
 * @param id - identificador do usuário
 * @param nome - nome do usuário
 * @param email - e-mail do usuário
 * @param cargo - cargo atual do usuário
 */
public record UsuarioResponseDTO(Long id, String nome, String email, UsuarioCargo cargo) {}
