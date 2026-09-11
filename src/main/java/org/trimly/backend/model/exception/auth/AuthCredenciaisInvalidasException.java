package org.trimly.backend.model.exception.auth;

/**
 * Lançada quando o e-mail não existe ou a senha não confere no login.
 */
public class AuthCredenciaisInvalidasException extends AuthException {
    /**
     * Cria a exceção com a mensagem fixa de credenciais inválidas.
     */
    public AuthCredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
