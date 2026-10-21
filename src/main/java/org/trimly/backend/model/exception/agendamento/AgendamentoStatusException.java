package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Alteração de status inválida: agendamento fora de `AGENDADO` ou status repetido.
 */
public class AgendamentoStatusException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoStatusException(String message) {
        super(message);
    }
}
