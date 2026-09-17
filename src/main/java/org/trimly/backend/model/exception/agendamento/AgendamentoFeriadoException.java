package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que a data selecionada coincide com um feriado nacional.
 */
public class AgendamentoFeriadoException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoFeriadoException(String message) {
        super(message);
    }
}
