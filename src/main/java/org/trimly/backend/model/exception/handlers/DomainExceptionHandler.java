package org.trimly.backend.model.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.TrimlyException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Traduz `TrimlyException` em `404`, `409` ou `422` por classe; novos subtipos herdam `422`.
 */
@Slf4j
@RestControllerAdvice
public class DomainExceptionHandler {
    /**
     * Mapeia a entidade não encontrada para `404`.
     *
     * @param exception - falha de entidade inexistente
     * @return ResponseEntity - resposta `404` com o corpo de erro
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(EntityNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Mapeia os conflitos de unicidade e de agendamento pendente para `409`.
     *
     * @param exception - falha de conflito da família `TrimlyException`
     * @return ResponseEntity - resposta `409` com o corpo de erro
     */
    @ExceptionHandler(
        {
                AgendamentoConflitoException.class,
                AgendamentoFeriadoException.class,
                DisponibilidadeConflitoException.class,
                ServicoNomeDuplicadoException.class,
                UsuarioEmailExistenteException.class,
                ServicoComAgendamentoPendenteException.class,
                UsuarioComAgendamentoPendenteException.class}
    )
    public ResponseEntity<ErrorResponseDTO> handleConflito(TrimlyException exception) {
        return build(HttpStatus.CONFLICT, exception);
    }

    /**
     * Mapeia qualquer outra {@link TrimlyException} para `422`.
     *
     * @param exception - falha de regra de domínio sem grupo específico
     * @return ResponseEntity - resposta `422` com o corpo de erro
     */
    @ExceptionHandler(TrimlyException.class)
    public ResponseEntity<ErrorResponseDTO> handleRegraDeDominio(TrimlyException exception) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, exception);
    }

    /**
     * Registra a falha de domínio no log e monta a resposta com o status indicado.
     *
     * @param status - status HTTP a devolver
     * @param exception - falha de domínio a reportar
     * @return ResponseEntity - resposta com o status e o corpo de erro
     */
    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, TrimlyException exception) {
        log.warn("domain error: {}", exception.getMessage(), exception);

        return ResponseEntity.status(status).body(ErrorResponseDTO.of(status, exception.getMessage()));
    }
}
