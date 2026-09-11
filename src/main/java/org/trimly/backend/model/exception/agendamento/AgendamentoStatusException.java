package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza uma alteração de status inválida: o agendamento não está em {@code AGENDADO} ou o novo
 * status é igual ao atual.
 */
public class AgendamentoStatusException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoStatusException(String message) {
        super(message);
    }
}
