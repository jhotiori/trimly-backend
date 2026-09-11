package org.trimly.backend.model.exception.auth;

/**
 * Lançada quando o token JWT recebido está ausente, malformado, expirado ou com
 * assinatura inválida.
 */
public class AuthTokenInvalidoException extends AuthException {
    /**
     * Cria a exceção com a mensagem fixa de token inválido ou expirado.
     */
    public AuthTokenInvalidoException() {
        super("Token inválido ou expirado");
    }
}
