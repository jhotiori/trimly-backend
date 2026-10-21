package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Remoção de serviço com agendamento `AGENDADO`.
 */
public class ServicoComAgendamentoPendenteException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public ServicoComAgendamentoPendenteException(String message) {
        super(message);
    }
}
