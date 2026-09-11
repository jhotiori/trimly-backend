package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que o agendamento ultrapassa o limite de um dia, com início e fim em datas diferentes.
 */
public class AgendamentoForaDoHorarioException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoForaDoHorarioException(String message) {
        super(message);
    }
}
