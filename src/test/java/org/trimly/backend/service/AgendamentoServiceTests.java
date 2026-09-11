package org.trimly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.AgendamentoEntity;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.service.agendamento.AgendamentoService;
import org.trimly.backend.model.service.agendamento.AgendamentoValidator;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTests {
    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private AgendamentoRepository repository;

    @Mock
    private AgendamentoMapper mapper;

    @Mock
    private AgendamentoValidator agendamentoValidator;

    @InjectMocks
    private AgendamentoService service;

    /**
     * Verifica que a criação executa os três validadores na ordem esperada e persiste o agendamento quando
     * nenhum deles lança exceção.
     */
    @Test
    void agendamento_TestCreateExecutaValidadoresEmOrdemEPersisteComSucesso() {
        AgendamentoCreateDTO request = new AgendamentoCreateDTO(LocalDate.of(2026, 9, 10), LocalTime.of(10, 0), 1L, 2L);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        ServicoEntity servico = new ServicoEntity();
        servico.setId(2L);
        servico.setDuracao(30);
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = inicio.plusMinutes(30);
        AgendamentoEntity mapeada = new AgendamentoEntity();
        mapeada.setData(inicio);
        mapeada.setDuracao(30);
        mapeada.setStatus(AgendamentoStatus.AGENDADO);
        mapeada.setUsuario(usuario);
        mapeada.setServico(servico);
        AgendamentoEntity salva = new AgendamentoEntity();
        salva.setId(9L);

        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(servicoService.findById(2L)).thenReturn(servico);
        when(mapper.toEntity(request, usuario, servico)).thenReturn(mapeada);
        when(repository.save(mapeada)).thenReturn(salva);

        AgendamentoEntity resultado = service.create(request);

        InOrder ordem = inOrder(agendamentoValidator);
        ordem.verify(agendamentoValidator).validateHorarioFuturo(inicio, fim);
        ordem.verify(agendamentoValidator).validateDisponibilidade(inicio, fim);
        ordem.verify(agendamentoValidator).validateConflitoDeHorario(null, inicio, fim);
        verify(repository).save(mapeada);
        assertThat(resultado).isEqualTo(salva);
    }

    /**
     * Verifica que uma atualização com todos os campos nulos é um no-op: retorna o agendamento inalterado sem
     * chamar nenhum validador nem o repositório.
     */
    @Test
    void agendamento_TestUpdateComDtoTodoNuloNaoChamaNenhumValidator() {
        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(1L);
        existente.setStatus(AgendamentoStatus.AGENDADO);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(null, null, null));

        assertThat(resultado).isEqualTo(existente);
        verifyNoInteractions(agendamentoValidator);
        verify(repository, never()).save(any());
    }

    /**
     * Verifica que uma atualização com um novo horário roda a validação de status seguida da cadeia de
     * horário/disponibilidade/conflito contra o horário de fim recalculado, e aplica a alteração.
     */
    @Test
    void agendamento_TestUpdateComNovoHorarioExecutaCadeiaDeValidacaoComFimRecalculado() {
        ServicoEntity servico = new ServicoEntity();
        servico.setId(2L);
        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(1L);
        existente.setData(LocalDateTime.of(2026, 9, 10, 10, 0));
        existente.setDuracao(30);
        existente.setStatus(AgendamentoStatus.AGENDADO);
        existente.setServico(servico);

        LocalDateTime novaData = LocalDateTime.of(2026, 9, 11, 14, 0);
        LocalDateTime novoFim = novaData.plusMinutes(30);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(novaData, null, null));

        verify(agendamentoValidator).validateStatusUpdate(existente, null);
        InOrder ordem = inOrder(agendamentoValidator);
        ordem.verify(agendamentoValidator).validateHorarioFuturo(novaData, novoFim);
        ordem.verify(agendamentoValidator).validateDisponibilidade(novaData, novoFim);
        ordem.verify(agendamentoValidator).validateConflitoDeHorario(eq(1L), eq(novaData), eq(novoFim));
        assertThat(resultado.getData()).isEqualTo(novaData);
        verify(repository).save(existente);
    }

    /**
     * Verifica que uma atualização de serviço recalcula a duração a partir do novo serviço antes de validar o
     * horário de fim.
     */
    @Test
    void agendamento_TestUpdateComNovoServicoRecalculaDuracaoAntesDeValidar() {
        ServicoEntity servicoAntigo = new ServicoEntity();
        servicoAntigo.setId(2L);
        ServicoEntity servicoNovo = new ServicoEntity();
        servicoNovo.setId(3L);
        servicoNovo.setDuracao(60);

        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(1L);
        existente.setData(LocalDateTime.of(2026, 9, 10, 10, 0));
        existente.setDuracao(30);
        existente.setStatus(AgendamentoStatus.AGENDADO);
        existente.setServico(servicoAntigo);

        LocalDateTime fimEsperado = existente.getData().plusMinutes(60);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoService.findById(3L)).thenReturn(servicoNovo);
        when(repository.save(existente)).thenReturn(existente);

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(null, null, 3L));

        verify(agendamentoValidator).validateHorarioFuturo(existente.getData(), fimEsperado);
        assertThat(resultado.getServico()).isEqualTo(servicoNovo);
        assertThat(resultado.getDuracao()).isEqualTo(60);
    }

    /**
     * Verifica que uma exceção lançada por um dos validadores durante a atualização é propagada sem persistir
     * o agendamento.
     */
    @Test
    void agendamento_TestUpdatePropagaExcecaoDoValidator() {
        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(1L);
        existente.setData(LocalDateTime.of(2026, 9, 10, 10, 0));
        existente.setDuracao(30);
        existente.setStatus(AgendamentoStatus.AGENDADO);
        existente.setServico(new ServicoEntity());

        LocalDateTime novaData = LocalDateTime.of(2026, 9, 10, 11, 0);
        LocalDateTime novoFim = novaData.plusMinutes(30);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new AgendamentoConflitoException("O horário escolhido já está ocupado por outro agendamento"))
                .when(agendamentoValidator)
                .validateConflitoDeHorario(1L, novaData, novoFim);

        assertThatThrownBy(() -> service.update(1L, new AgendamentoUpdateDTO(novaData, null, null)))
                .isInstanceOf(AgendamentoConflitoException.class);
        verify(repository, never()).save(any());
    }
}
