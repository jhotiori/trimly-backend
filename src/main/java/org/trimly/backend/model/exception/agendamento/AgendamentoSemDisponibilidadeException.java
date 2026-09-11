package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que nenhuma janela de disponibilidade do dia comporta o horário do agendamento.
 */
public class AgendamentoSemDisponibilidadeException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoSemDisponibilidadeException(String message) {
        super(message);
    }
}
