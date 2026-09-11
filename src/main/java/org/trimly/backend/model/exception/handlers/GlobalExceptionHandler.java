package org.trimly.backend.model.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Trata as falhas que não pertencem ao domínio da aplicação (a família
 * {@code TrimlyException}, coberta por {@code DomainExceptionHandler}), devolvendo
 * um {@code 500} genérico sem revelar detalhes da exceção ao cliente.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Registra a falha inesperada no log e devolve {@code 500} com mensagem genérica.
     *
     * @param exception - falha não pertencente ao domínio
     * @return ResponseEntity - resposta {@code 500} com o corpo de erro
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception exception) {
        log.error("unexpected error: {}", exception);

        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro inesperado ao processar a requisição");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
