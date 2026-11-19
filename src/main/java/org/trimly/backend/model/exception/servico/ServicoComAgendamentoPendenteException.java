package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Remoção de serviço com agendamento `AGENDADO`.
 */
public class ServicoComAgendamentoPendenteException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public ServicoComAgendamentoPendenteException() {
        super("Não é possível remover serviço com agendamento pendente");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public ServicoComAgendamentoPendenteException(String message) {
        super(message);
    }
}
