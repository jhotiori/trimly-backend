package org.trimly.backend.model.exception.auth;

/**
 * E-mail inexistente ou senha incorreta no login.
 */
public class AuthCredenciaisInvalidasException extends AuthException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AuthCredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AuthCredenciaisInvalidasException(String message) {
        super(message);
    }
}
