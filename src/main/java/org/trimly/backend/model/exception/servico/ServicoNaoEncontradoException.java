package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Serviço inexistente.
 */
public class ServicoNaoEncontradoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public ServicoNaoEncontradoException() {
        super("Serviço não encontrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public ServicoNaoEncontradoException(String message) {
        super(message);
    }
}
