package org.trimly.backend.model.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.model.exception.TrimlyException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoNaoEncontradoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoRecessoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeNaoEncontradaException;
import org.trimly.backend.model.exception.feriado.FeriadosIndisponiveisException;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNaoEncontradoException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoNaoEncontradoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioNaoEncontradoException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Traduz `TrimlyException` em `404`, `409`, `422` ou `503` por classe; novos subtipos herdam `422`.
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
    @ExceptionHandler({
            AgendamentoNaoEncontradoException.class,
            DisponibilidadeNaoEncontradaException.class,
            RecessoNaoEncontradoException.class,
            ServicoNaoEncontradoException.class,
            UsuarioNaoEncontradoException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleNotFound(TrimlyException exception) {
        return build(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Mapeia os conflitos de unicidade e de agendamento pendente para `409`.
     *
     * @param exception - falha de conflito da família `TrimlyException`
     * @return ResponseEntity - resposta `409` com o corpo de erro
     */
    @ExceptionHandler({
            AgendamentoConflitoException.class,
            AgendamentoFeriadoException.class,
            AgendamentoRecessoException.class,
            DisponibilidadeConflitoException.class,
            RecessoConflitoException.class,
            ServicoNomeDuplicadoException.class,
            UsuarioEmailExistenteException.class,
            ServicoComAgendamentoPendenteException.class,
            UsuarioComAgendamentoPendenteException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflito(TrimlyException exception) {
        return build(HttpStatus.CONFLICT, exception);
    }

    /**
     * Mapeia a indisponibilidade da API de feriados para `503`.
     *
     * @param exception - falha da integração com a BrasilAPI
     * @return ResponseEntity - resposta `503` com o corpo de erro
     */
    @ExceptionHandler(FeriadosIndisponiveisException.class)
    public ResponseEntity<ErrorResponseDTO> handleIndisponivel(TrimlyException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, exception);
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
