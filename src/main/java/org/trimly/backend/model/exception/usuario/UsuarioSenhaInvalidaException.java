package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Senha abaixo do tamanho mínimo.
 */
public class UsuarioSenhaInvalidaException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioSenhaInvalidaException() {
        super("Senha deve ter no mínimo 6 caracteres");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioSenhaInvalidaException(String message) {
        super(message);
    }
}
