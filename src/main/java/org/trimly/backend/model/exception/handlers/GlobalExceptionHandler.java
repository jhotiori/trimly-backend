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
 * Trata as falhas que não pertencem ao domínio da aplicação (a família
 * {@code TrimlyException}, coberta por {@code DomainExceptionHandler}).
 *
 * Requisições mal formadas (corpo ilegível, parâmetro com valor inválido ou falha de
 * {@code @Valid}) viram {@code 400}; qualquer outra falha vira um {@code 500} genérico sem
 * revelar detalhes da exceção ao cliente.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Mapeia a falha de {@code @Valid} no corpo da requisição para {@code 400}, reunindo em ordem
     * alfabética as mensagens de validação dos campos rejeitados.
     *
     * @param exception - falha de validação do corpo da requisição
     * @return ResponseEntity - resposta {@code 400} com o corpo de erro
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
     * tipo do campo) para {@code 400}.
     *
     * @param exception - falha de leitura do corpo da requisição
     * @return ResponseEntity - resposta {@code 400} com o corpo de erro
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleCorpoInvalido(HttpMessageNotReadableException exception) {
        return badRequest(exception, "Corpo da requisição ausente ou mal formatado");
    }

    /**
     * Mapeia o parâmetro de caminho ou de consulta que não converte para o tipo esperado para
     * {@code 400}.
     *
     * @param exception - falha de conversão do parâmetro
     * @return ResponseEntity - resposta {@code 400} com o corpo de erro
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroInvalido(MethodArgumentTypeMismatchException exception) {
        return badRequest(exception, "Parâmetro da requisição com valor inválido");
    }

    /**
     * Registra a falha inesperada no log e devolve {@code 500} com mensagem genérica.
     *
     * @param exception - falha não pertencente ao domínio
     * @return ResponseEntity - resposta {@code 500} com o corpo de erro
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception exception) {
        log.error("unexpected error: {}", exception.getMessage(), exception);

        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro inesperado ao processar a requisição"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Registra a requisição mal formada no log e monta a resposta {@code 400}.
     *
     * @param exception - falha da requisição a reportar
     * @param message - mensagem devolvida ao cliente
     * @return ResponseEntity - resposta {@code 400} com o corpo de erro
     */
    private ResponseEntity<ErrorResponseDTO> badRequest(Exception exception, String message) {
        log.warn("bad request: {}", exception.toString());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponseDTO response = new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);

        return ResponseEntity.status(status).body(response);
    }
}
