package org.trimly.backend.view.dto.usuario;

/**
 * Resultado de um login por credenciais. O campo {@code usuario} é nulo quando {@code sucesso} é
 * {@code false}.
 *
 * @param sucesso - indica se as credenciais conferem
 * @param usuario - usuário autenticado, ou nulo quando o login falha
 */
public record UsuarioLoginResponseDTO(boolean sucesso, UsuarioResponseDTO usuario) {}
