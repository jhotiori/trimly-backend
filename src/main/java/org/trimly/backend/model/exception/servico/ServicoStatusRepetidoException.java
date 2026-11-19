package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Atualização para o status atual do serviço.
 */
public class ServicoStatusRepetidoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public ServicoStatusRepetidoException() {
        super("Serviço já está nesse status");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public ServicoStatusRepetidoException(String message) {
        super(message);
    }
}
