package org.trimly.backend.model.exception.disponibilidade;

/**
 * Sinaliza que a janela informada se sobrepõe a outra disponibilidade no mesmo dia da semana.
 */
public class DisponibilidadeConflitoException extends DisponibilidadeException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeConflitoException(String message) {
        super(message);
    }
}
