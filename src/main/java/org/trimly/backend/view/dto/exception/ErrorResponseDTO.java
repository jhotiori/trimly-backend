package org.trimly.backend.view.dto.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

/**
 * Corpo padrão das respostas de erro da API.
 *
 * @param status - código HTTP
 * @param error - frase associada ao código HTTP
 * @param message - mensagem descritiva do erro
 */
public record ErrorResponseDTO(
        @Schema(description = "Código HTTP da resposta", example = "404") Integer status,

        @Schema(description = "Frase padrão do código HTTP", example = "Not Found") String error,

        @Schema(description = "Mensagem fixa do erro", example = "Agendamento não foi encontrado") String message
) {
    /**
     * Monta o corpo de erro a partir do status HTTP informado.
     *
     * @param status - status HTTP da resposta
     * @param message - mensagem descritiva do erro
     * @return ErrorResponseDTO - o corpo de erro correspondente
     */
    public static ErrorResponseDTO of(HttpStatus status, String message) {
        return new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);
    }
}
