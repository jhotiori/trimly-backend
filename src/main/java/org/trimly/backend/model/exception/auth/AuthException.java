package org.trimly.backend.model.exception.auth;

/**
 * Raiz das falhas de autenticação, sempre `401`.
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
