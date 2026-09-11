package org.trimly.backend.model.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.model.exception.auth.AuthException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Traduz a família {@code AuthException} em {@code 401}.
 *
 * Fica à frente de {@code DomainExceptionHandler} e {@code GlobalExceptionHandler}
 * porque as falhas de autenticação não pertencem ao domínio e nunca devem cair no
 * {@code 500} genérico. A mensagem devolvida ao cliente é fixa, sem revelar se foi
 * o e-mail ou a senha que falhou; o detalhe completo fica no log do servidor.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthenticationExceptionHandler {

    /**
     * Registra a falha de autenticação e devolve {@code 401} com mensagem fixa.
     *
     * @param exception - falha da família {@code AuthException}
     * @return ResponseEntity - resposta {@code 401} com o corpo de erro
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuth(AuthException exception) {
        log.warn("auth error: {}", exception.toString());

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponseDTO response =
                new ErrorResponseDTO(status.value(), status.getReasonPhrase(), exception.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}
