package org.trimly.backend.model.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.TrimlyException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Traduz a família {@code TrimlyException} em um status HTTP significativo.
 *
 * O mapeamento é feito por classe, sem campo de status na exceção nem mapa
 * classe-para-status: {@link EntityNotFoundException} vira {@code 404}, os conflitos
 * de unicidade e de horário viram {@code 409}, e qualquer outra
 * {@link TrimlyException} cai no {@code 422}. Um novo subtipo herda o {@code 422}
 * por padrão e só precisa entrar em um grupo para ser {@code 404} ou {@code 409}.
 */
@Slf4j
@RestControllerAdvice
public class DomainExceptionHandler {

    /**
     * Mapeia a entidade não encontrada para {@code 404}.
     *
     * @param exception - falha de entidade inexistente
     * @return ResponseEntity - resposta {@code 404} com o corpo de erro
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(EntityNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Mapeia os conflitos de unicidade e de agendamento pendente para {@code 409}.
     *
     * @param exception - falha de conflito da família {@code TrimlyException}
     * @return ResponseEntity - resposta {@code 409} com o corpo de erro
     */
    @ExceptionHandler({
        AgendamentoConflitoException.class,
        ServicoNomeDuplicadoException.class,
        UsuarioEmailExistenteException.class,
        ServicoComAgendamentoPendenteException.class,
        UsuarioComAgendamentoPendenteException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflito(TrimlyException exception) {
        return build(HttpStatus.CONFLICT, exception);
    }

    /**
     * Mapeia qualquer outra {@link TrimlyException} para {@code 422}.
     *
     * @param exception - falha de regra de domínio sem grupo específico
     * @return ResponseEntity - resposta {@code 422} com o corpo de erro
     */
    @ExceptionHandler(TrimlyException.class)
    public ResponseEntity<ErrorResponseDTO> handleRegraDeDominio(TrimlyException exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    /**
     * Registra a falha de domínio no log e monta a resposta com o status indicado.
     *
     * @param status - status HTTP a devolver
     * @param exception - falha de domínio a reportar
     * @return ResponseEntity - resposta com o status e o corpo de erro
     */
    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, TrimlyException exception) {
        log.warn("domain error: {}", exception);

        ErrorResponseDTO response =
                new ErrorResponseDTO(status.value(), status.getReasonPhrase(), exception.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}
