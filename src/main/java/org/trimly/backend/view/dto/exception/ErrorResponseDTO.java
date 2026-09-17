package org.trimly.backend.view.dto.exception;

import io.swagger.v3.oas.annotations.media.Schema;

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

        @Schema(
                description = "Descrição fixa do erro, sem dados da requisição",
                example = "Agendamento não foi encontrado"
        ) String message
) {}
