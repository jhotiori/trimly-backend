package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Início do agendamento além do limite de 14 dias.
 */
public class AgendamentoAntecedenciaExcedidaException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoAntecedenciaExcedidaException(String message) {
        super(message);
    }
}
