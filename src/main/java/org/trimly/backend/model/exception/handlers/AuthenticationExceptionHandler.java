package org.trimly.backend.model.exception.handlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.infra.security.SecurityErrorHandler;
import org.trimly.backend.model.exception.auth.AuthException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Traduz `AuthException` em `401` e `AccessDeniedException` em `403`, com mensagem fixa, à frente dos demais handlers.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthenticationExceptionHandler {
    /**
     * Registra a falha de autenticação e devolve `401` com mensagem fixa.
     *
     * @param exception - falha da família `AuthException`
     * @return ResponseEntity - resposta `401` com o corpo de erro
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuth(AuthException exception) {
        log.warn("auth error: {}", exception.toString());

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(ErrorResponseDTO.of(status, exception.getMessage()));
    }

    /**
     * Registra a negação de `@PreAuthorize` e devolve `403` com mensagem fixa, em vez do `500` do handler genérico.
     *
     * @param request - requisição HTTP negada
     * @return ResponseEntity - resposta `403` com o corpo de erro
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(HttpServletRequest request) {
        SecurityErrorHandler.logAcessoNegado(request);

        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(ErrorResponseDTO.of(status, SecurityErrorHandler.ACESSO_NEGADO));
    }
}
