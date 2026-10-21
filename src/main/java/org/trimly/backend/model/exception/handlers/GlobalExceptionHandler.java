package org.trimly.backend.model.exception.handlers;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Trata falhas fora do domínio: requisição mal formada vira `400` e o restante `500` genérico.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Mapeia a falha de `@Valid` no corpo da requisição para `400`, reunindo em ordem
     * alfabética as mensagens de validação dos campos rejeitados.
     *
     * @param exception - falha de validação do corpo da requisição
     * @return ResponseEntity - resposta `400` com o corpo de erro
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidacao(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining("; "));

        return badRequest(exception, message);
    }

    /**
     * Mapeia o corpo de requisição ausente ou ilegível (JSON mal formado ou valor incompatível com o
     * tipo do campo) para `400`.
     *
     * @param exception - falha de leitura do corpo da requisição
     * @return ResponseEntity - resposta `400` com o corpo de erro
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleCorpoInvalido(HttpMessageNotReadableException exception) {
        return badRequest(exception, "Corpo da requisição ausente ou mal formatado");
    }

    /**
     * Mapeia o parâmetro de caminho ou de consulta que não converte para o tipo esperado para
     * `400`.
     *
     * @param exception - falha de conversão do parâmetro
     * @return ResponseEntity - resposta `400` com o corpo de erro
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroInvalido(MethodArgumentTypeMismatchException exception) {
        return badRequest(exception, "Parâmetro da requisição com valor inválido");
    }

    /**
     * Registra a falha inesperada no log e devolve `500` com mensagem genérica.
     *
     * @param exception - falha não pertencente ao domínio
     * @return ResponseEntity - resposta `500` com o corpo de erro
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception exception) {
        log.error("unexpected error: {}", exception.getMessage(), exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ErrorResponseDTO.of(status, "Ocorreu um erro inesperado ao processar a requisição"));
    }

    /**
     * Registra a requisição mal formada no log e monta a resposta `400`.
     *
     * @param exception - falha da requisição a reportar
     * @param message - mensagem devolvida ao cliente
     * @return ResponseEntity - resposta `400` com o corpo de erro
     */
    private ResponseEntity<ErrorResponseDTO> badRequest(Exception exception, String message) {
        log.warn("bad request: {}", exception.toString());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ErrorResponseDTO.of(status, message));
    }
}
