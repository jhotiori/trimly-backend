package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Atualização para o cargo atual do usuário.
 */
public class UsuarioCargoRepetidoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioCargoRepetidoException() {
        super("Usuário já possui esse cargo");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioCargoRepetidoException(String message) {
        super(message);
    }
}
