package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que o início do agendamento ultrapassa o limite de antecedência de 14 dias.
 */
public class AgendamentoAntecedenciaExcedidaException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoAntecedenciaExcedidaException(String message) {
        super(message);
    }
}
