package org.trimly.backend.model.entity.usuario;

/**
 * Cargo de um usuário, que define seu nível de acesso. {@code CLIENTE} agenda os próprios serviços;
 * {@code ADMIN} e {@code DONO} administram a barbearia.
 */
public enum UsuarioCargo {
    CLIENTE,
    ADMIN,
    DONO
}
