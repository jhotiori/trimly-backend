package org.trimly.backend.model.exception.auth;

/**
 * E-mail inexistente ou senha incorreta no login.
 */
public class AuthCredenciaisInvalidasException extends AuthException {
    /**
     * Cria a exceção com a mensagem fixa de credenciais inválidas.
     */
    public AuthCredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
