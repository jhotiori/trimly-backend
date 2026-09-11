package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que o horário escolhido se sobrepõe a outro agendamento em {@code AGENDADO} no mesmo dia.
 */
public class AgendamentoConflitoException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoConflitoException(String message) {
        super(message);
    }
}
