package org.trimly.backend.model.service.servico;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.ServicoRepository;

/**
 * Testes para {@link ServicoValidator}.
 */
@ExtendWith(MockitoExtension.class)
class ServicoValidatorTests {
    /**
     * Repositório de serviços simulado.
     * @see {@link ServicoRepository}
     */
    @Mock
    private ServicoRepository repository;

    /**
     * Repositório de agendamentos simulado.
     * @see {@link AgendamentoRepository}
     */
    @Mock
    private AgendamentoRepository agendamentoRepository;

    /**
     * Validador sob teste, com os mocks injetados.
     * @see {@link ServicoValidator}
     */
    @InjectMocks
    private ServicoValidator validator;

    @Test
    @DisplayName("Aceita nome ainda não cadastrado")
    void testeDadoNomeLivreQuandoValidarNomeUnicoEntaoNaoLanca() {
        when(repository.existsByNomeIgnoreCase("Corte")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateNomeUnico("Corte"));
    }

    @Test
    @DisplayName("Rejeita nome já cadastrado")
    void testeDadoNomeEmUsoQuandoValidarNomeUnicoEntaoLancaNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("Corte")).thenReturn(true);

        assertThrows(ServicoNomeDuplicadoException.class, () -> validator.validateNomeUnico("Corte"));
    }

    @Test
    @DisplayName("Aceita atualização sem novo status e não consulta agendamentos")
    void testeDadoStatusNuloQuandoValidarStatusEntaoNaoLancaNemConsultaAgendamentos() {
        assertDoesNotThrow(() -> validator.validateStatusUpdate(servico(ServicoStatus.ATIVO), null));

        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    @DisplayName("Rejeita repetir o status atual")
    void testeDadoMesmoStatusQuandoValidarStatusEntaoLancaStatusRepetido() {
        ServicoEntity ativo = servico(ServicoStatus.ATIVO);

        ServicoException excecao = assertThrows(
                ServicoException.class,
                () -> validator.validateStatusUpdate(ativo, ServicoStatus.ATIVO)
        );

        assertEquals("O serviço já está nesse status", excecao.getMessage());
    }

    @Test
    @DisplayName("Rejeita desativar serviço com agendamentos futuros")
    void testeDadoAgendamentosFuturosQuandoDesativarEntaoLancaExcecao() {
        when(
                agendamentoRepository.existsByServicoIdAndStatusAndDataAfter(
                        eq(1L),
                        eq(AgendamentoStatus.AGENDADO),
                        any(LocalDateTime.class)
                )
        ).thenReturn(true);

        ServicoEntity ativo = servico(ServicoStatus.ATIVO);

        ServicoException excecao = assertThrows(
                ServicoException.class,
                () -> validator.validateStatusUpdate(ativo, ServicoStatus.INATIVO)
        );

        assertEquals("O serviço não pode ser desativado enquanto tiver agendamentos futuros", excecao.getMessage());
    }

    @Test
    @DisplayName("Aceita desativar serviço sem agendamentos futuros")
    void testeDadoSemAgendamentosFuturosQuandoDesativarEntaoNaoLanca() {
        when(
                agendamentoRepository.existsByServicoIdAndStatusAndDataAfter(
                        eq(1L),
                        eq(AgendamentoStatus.AGENDADO),
                        any(LocalDateTime.class)
                )
        ).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateStatusUpdate(servico(ServicoStatus.ATIVO), ServicoStatus.INATIVO));
    }

    @Test
    @DisplayName("Aceita reativar serviço sem consultar agendamentos")
    void testeDadoServicoInativoQuandoReativarEntaoNaoLancaNemConsultaAgendamentos() {
        assertDoesNotThrow(() -> validator.validateStatusUpdate(servico(ServicoStatus.INATIVO), ServicoStatus.ATIVO));

        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    @DisplayName("Rejeita remover serviço com agendamento pendente")
    void testeDadoAgendamentoPendenteQuandoValidarRemocaoEntaoLancaPendente() {
        when(agendamentoRepository.existsByServicoIdAndStatus(1L, AgendamentoStatus.AGENDADO)).thenReturn(true);

        assertThrows(ServicoComAgendamentoPendenteException.class, () -> validator.validateSemAgendamentoPendente(1L));
    }

    @Test
    @DisplayName("Aceita remover serviço sem agendamento pendente")
    void testeDadoSemAgendamentoPendenteQuandoValidarRemocaoEntaoNaoLanca() {
        when(agendamentoRepository.existsByServicoIdAndStatus(1L, AgendamentoStatus.AGENDADO)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateSemAgendamentoPendente(1L));
    }

    private static ServicoEntity servico(ServicoStatus status) {
        return ServicoEntity.builder().id(1L).nome("Corte").status(status).build();
    }
}
