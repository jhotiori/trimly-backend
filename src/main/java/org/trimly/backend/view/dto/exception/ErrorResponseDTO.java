package org.trimly.backend.view.dto.exception;

/**
 * Corpo padrão das respostas de erro da API.
 *
 * @param status - código HTTP
 * @param error - frase associada ao código HTTP
 * @param message - mensagem descritiva do erro
 */
public record ErrorResponseDTO(Integer status, String error, String message) {}
