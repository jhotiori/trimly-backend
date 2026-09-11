package org.trimly.backend.view.dto.auth;

/**
 * Resposta de autenticação, contendo o token JWT emitido.
 *
 * @param token - token JWT assinado
 */
public record AuthResponseDTO(String token) {}
