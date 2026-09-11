package org.trimly.backend.service.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.entity.enums.ServicoStatus;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.ServicoRepository;
import org.trimly.backend.model.service.servico.ServicoValidator;

@ExtendWith(MockitoExtension.class)
class ServicoValidatorTests {
    @Mock
    private ServicoRepository repository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private ServicoValidator validator;

    /**
     * Verifica que um nome ainda não utilizado por nenhum serviço não lança exceção.
     */
    @Test
    void servico_TestValidateNomeUnicoComNomeDisponivelNaoLancaExcecao() {
        when(repository.existsByNomeIgnoreCase("Corte")).thenReturn(false);

        assertThatCode(() -> validator.validateNomeUnico("Corte")).doesNotThrowAnyException();
    }

    /**
     * Verifica que um nome já utilizado por outro serviço lança {@link ServicoNomeDuplicadoException}.
     */
    @Test
    void servico_TestValidateNomeUnicoComNomeDuplicadoLancaExcecao() {
        when(repository.existsByNomeIgnoreCase("Corte")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateNomeUnico("Corte"))
                .isInstanceOf(ServicoNomeDuplicadoException.class);
    }

    /**
     * Verifica que um status nulo não altera o serviço nem consulta os repositórios.
     */
    @Test
    void servico_TestValidateStatusUpdateComNovoStatusNuloNaoLancaExcecao() {
        ServicoEntity servico = new ServicoEntity();
        servico.setStatus(ServicoStatus.ATIVO);

        assertThatCode(() -> validator.validateStatusUpdate(servico, null)).doesNotThrowAnyException();
        verify(agendamentoRepository, never()).existsByServicoIdAndStatusAndDataAfter(anyLong(), any(), any());
    }

    /**
     * Verifica que solicitar o mesmo status já atual do serviço lança {@link ServicoException}.
     */
    @Test
    void servico_TestValidateStatusUpdateComMesmoStatusLancaExcecao() {
        ServicoEntity servico = new ServicoEntity();
        servico.setStatus(ServicoStatus.ATIVO);

        assertThatThrownBy(() -> validator.validateStatusUpdate(servico, ServicoStatus.ATIVO))
                .isInstanceOf(ServicoException.class);
    }

    /**
     * Verifica que desativar um serviço com agendamento futuro em {@code AGENDADO} lança {@link ServicoException}.
     */
    @Test
    void servico_TestValidateStatusUpdateDesativandoComAgendamentoFuturoLancaExcecao() {
        ServicoEntity servico = new ServicoEntity();
        servico.setId(1L);
        servico.setStatus(ServicoStatus.ATIVO);

        when(agendamentoRepository.existsByServicoIdAndStatusAndDataAfter(
                        eq(1L), eq(AgendamentoStatus.AGENDADO), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validateStatusUpdate(servico, ServicoStatus.INATIVO))
                .isInstanceOf(ServicoException.class);
    }

    /**
     * Verifica que desativar um serviço sem nenhum agendamento futuro não lança exceção.
     */
    @Test
    void servico_TestValidateStatusUpdateDesativandoSemAgendamentoFuturoNaoLancaExcecao() {
        ServicoEntity servico = new ServicoEntity();
        servico.setId(1L);
        servico.setStatus(ServicoStatus.ATIVO);

        when(agendamentoRepository.existsByServicoIdAndStatusAndDataAfter(
                        eq(1L), eq(AgendamentoStatus.AGENDADO), any()))
                .thenReturn(false);

        assertThatCode(() -> validator.validateStatusUpdate(servico, ServicoStatus.INATIVO))
                .doesNotThrowAnyException();
    }

    /**
     * Verifica que um serviço com agendamento pendente em {@code AGENDADO} lança
     * {@link ServicoComAgendamentoPendenteException}.
     */
    @Test
    void servico_TestValidateSemAgendamentoPendenteComAgendamentoPendenteLancaExcecao() {
        when(agendamentoRepository.existsByServicoIdAndStatus(1L, AgendamentoStatus.AGENDADO))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validateSemAgendamentoPendente(1L))
                .isInstanceOf(ServicoComAgendamentoPendenteException.class);
    }

    /**
     * Verifica que um serviço sem nenhum agendamento pendente não lança exceção.
     */
    @Test
    void servico_TestValidateSemAgendamentoPendenteSemAgendamentoPendenteNaoLancaExcecao() {
        when(agendamentoRepository.existsByServicoIdAndStatus(1L, AgendamentoStatus.AGENDADO))
                .thenReturn(false);

        assertThatCode(() -> validator.validateSemAgendamentoPendente(1L)).doesNotThrowAnyException();
    }
}
