package org.trimly.backend.model.exception.usuario;

/**
 * Sinaliza a tentativa de usar um e-mail já cadastrado para outro usuário.
 */
public class UsuarioEmailExistenteException extends UsuarioException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public UsuarioEmailExistenteException(String message) {
        super(message);
    }
}
