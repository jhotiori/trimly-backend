package org.trimly.backend.model.exception.servico;

/**
 * Sinaliza a tentativa de remover um serviço que ainda tem agendamento em {@code AGENDADO}.
 */
public class ServicoComAgendamentoPendenteException extends ServicoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public ServicoComAgendamentoPendenteException(String message) {
        super(message);
    }
}
