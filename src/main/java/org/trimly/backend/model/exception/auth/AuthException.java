package org.trimly.backend.model.exception.auth;

/**
 * Raiz das falhas de autenticação.
 *
 * Diferente da família {@code TrimlyException}, que cobre regras de domínio, esta
 * hierarquia é tratada por {@code AuthenticationExceptionHandler} e sempre resulta
 * em {@code 401}.
 */
public class AuthException extends RuntimeException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AuthException(String message) {
        super(message);
    }
}
