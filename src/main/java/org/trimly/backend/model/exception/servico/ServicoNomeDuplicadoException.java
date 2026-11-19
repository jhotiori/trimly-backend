package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Nome de serviço já usado por outro.
 */
public class ServicoNomeDuplicadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public ServicoNomeDuplicadoException() {
        super("Nome de serviço já cadastrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public ServicoNomeDuplicadoException(String message) {
        super(message);
    }
}
