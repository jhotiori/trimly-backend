package org.trimly.backend.model.service.agendamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Testes para {@link AgendamentoService}.
 */
@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTests {
    /**
     * Início base dos cenários.
     */
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 1, 7, 9, 0);

    /**
     * Fim base dos cenários, 30 minutos após o início.
     */
    private static final LocalDateTime FIM = INICIO.plusMinutes(30);

    /**
     * Serviço de usuários simulado.
     * @see {@link UsuarioService}
     */
    @Mock
    private UsuarioService usuarioService;

    /**
     * Serviço de serviços simulado.
     * @see {@link ServicoService}
     */
    @Mock
    private ServicoService servicoService;

    /**
     * Repositório de agendamentos simulado.
     * @see {@link AgendamentoRepository}
     */
    @Mock
    private AgendamentoRepository repository;

    /**
     * Mapper de agendamentos simulado.
     * @see {@link AgendamentoMapper}
     */
    @Mock
    private AgendamentoMapper mapper;

    /**
     * Validador de agendamentos simulado.
     * @see {@link AgendamentoValidator}
     */
    @Mock
    private AgendamentoValidator agendamentoValidator;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link AgendamentoService}
     */
    @InjectMocks
    private AgendamentoService service;

    @Test
    @DisplayName("Valida o horário na ordem das regras e salva o agendamento")
    void testeDadoHorarioValidoQuandoCriarEntaoValidaNaOrdemESalva() {
        AgendamentoCreateDTO request = requisicaoDeCriacao();
        AgendamentoEntity entity = prepararCriacao(request);
        when(repository.save(entity)).thenReturn(entity);

        AgendamentoEntity resultado = service.create(request);

        assertSame(entity, resultado);
        InOrder ordem = inOrder(agendamentoValidator);
        ordem.verify(agendamentoValidator).validateIsFeriado(INICIO.toLocalDate());
        ordem.verify(agendamentoValidator).validateHorarioFuturo(INICIO, FIM);
        ordem.verify(agendamentoValidator).validateLimiteAntecedencia(INICIO);
        ordem.verify(agendamentoValidator).validateDisponibilidade(INICIO, FIM);
        ordem.verify(agendamentoValidator).validateConflitoDeHorario(null, INICIO, FIM);
    }

    @Test
    @DisplayName("Interrompe na checagem de feriado antes das demais regras")
    void testeDadoFeriadoQuandoCriarEntaoLancaExcecaoSemValidarOutrasRegrasNemSalvar() {
        AgendamentoCreateDTO request = requisicaoDeCriacao();
        prepararCriacao(request);
        doThrow(new AgendamentoFeriadoException("Feriado")).when(agendamentoValidator)
                .validateIsFeriado(INICIO.toLocalDate());

        assertThrows(AgendamentoFeriadoException.class, () -> service.create(request));

        verify(agendamentoValidator).validateIsFeriado(INICIO.toLocalDate());
        verifyNoMoreInteractions(agendamentoValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando há conflito de horário")
    void testeDadoConflitoDeHorarioQuandoCriarEntaoLancaExcecaoSemSalvar() {
        AgendamentoCreateDTO request = requisicaoDeCriacao();
        prepararCriacao(request);
        doThrow(new AgendamentoConflitoException("Horário ocupado")).when(agendamentoValidator)
                .validateConflitoDeHorario(null, INICIO, FIM);

        assertThrows(AgendamentoConflitoException.class, () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não consulta o serviço nem valida quando o usuário não existe")
    void testeDadoUsuarioInexistenteQuandoCriarEntaoLancaNaoEncontradoSemConsultarServico() {
        AgendamentoCreateDTO request = requisicaoDeCriacao();
        when(usuarioService.findById(2L)).thenThrow(new EntityNotFoundException("Usuario não foi encontrado"));

        assertThrows(EntityNotFoundException.class, () -> service.create(request));

        verifyNoInteractions(servicoService, mapper, agendamentoValidator, repository);
    }

    @Test
    @DisplayName("Não valida nem salva quando o serviço não existe")
    void testeDadoServicoInexistenteQuandoCriarEntaoLancaNaoEncontradoSemValidar() {
        AgendamentoCreateDTO request = requisicaoDeCriacao();
        when(usuarioService.findById(2L)).thenReturn(usuario());
        when(servicoService.findById(1L)).thenThrow(new EntityNotFoundException("Serviço não encontrado"));

        assertThrows(EntityNotFoundException.class, () -> service.create(request));

        verifyNoInteractions(mapper, agendamentoValidator, repository);
    }

    @Test
    @DisplayName("Valida a nova data com a duração atual e ignora o próprio id no conflito")
    void testeDadoNovaDataQuandoAtualizarEntaoRevalidaHorarioESalva() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        LocalDateTime novaData = INICIO.plusDays(1);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(novaData, null, null));

        assertEquals(novaData, resultado.getData());
        verify(agendamentoValidator).validateStatusUpdate(entity, null);
        verify(agendamentoValidator).validateConflitoDeHorario(1L, novaData, novaData.plusMinutes(30));
    }

    @Test
    @DisplayName("Adota a duração do novo serviço ao trocá-lo")
    void testeDadoNovoServicoQuandoAtualizarEntaoAdotaDuracaoDoNovoServico() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        ServicoEntity novoServico = servico(5L, 60);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(servicoService.findById(5L)).thenReturn(novoServico);
        when(repository.save(entity)).thenReturn(entity);

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(null, null, 5L));

        assertAll(
                () -> assertSame(novoServico, resultado.getServico()),
                () -> assertEquals(60, resultado.getDuracao())
        );
        verify(agendamentoValidator).validateDisponibilidade(INICIO, INICIO.plusMinutes(60));
    }

    @Test
    @DisplayName("Aplica o novo status depois de validá-lo")
    void testeDadoNovoStatusQuandoAtualizarEntaoValidaStatusEAplica() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        AgendamentoEntity resultado = service
                .update(1L, new AgendamentoUpdateDTO(null, AgendamentoStatus.CANCELADO, null));

        assertEquals(AgendamentoStatus.CANCELADO, resultado.getStatus());
        verify(agendamentoValidator).validateStatusUpdate(entity, AgendamentoStatus.CANCELADO);
    }

    @Test
    @DisplayName("Não escreve nem valida quando todos os campos são nulos")
    void testeDadoTodosOsCamposNulosQuandoAtualizarEntaoRetornaSemAlterar() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        AgendamentoEntity resultado = service.update(1L, new AgendamentoUpdateDTO(null, null, null));

        assertSame(entity, resultado);
        verifyNoInteractions(agendamentoValidator, servicoService);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Interrompe na validação de status antes das regras de horário")
    void testeDadoStatusInvalidoQuandoAtualizarEntaoLancaExcecaoSemValidarHorarioNemSalvar() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new AgendamentoStatusException("O agendamento já está nesse status")).when(agendamentoValidator)
                .validateStatusUpdate(entity, AgendamentoStatus.AGENDADO);

        AgendamentoUpdateDTO request = new AgendamentoUpdateDTO(null, AgendamentoStatus.AGENDADO, null);

        assertThrows(AgendamentoStatusException.class, () -> service.update(1L, request));

        verify(agendamentoValidator).validateStatusUpdate(entity, AgendamentoStatus.AGENDADO);
        verifyNoMoreInteractions(agendamentoValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Lança exceção ao atualizar agendamento inexistente")
    void testeDadoAgendamentoInexistenteQuandoAtualizarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        AgendamentoUpdateDTO request = new AgendamentoUpdateDTO(null, AgendamentoStatus.CANCELADO, null);

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
    }

    @Test
    @DisplayName("Remove agendamento existente")
    void testeDadoAgendamentoExistenteQuandoDeletarEntaoRemove() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteById(1L);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Lança exceção ao remover agendamento inexistente")
    void testeDadoAgendamentoInexistenteQuandoDeletarEntaoLancaNaoEncontradoSemRemover() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));

        verify(repository, never()).delete(any(AgendamentoEntity.class));
    }

    @Test
    @DisplayName("Lista agendamentos aplicando o filtro")
    void testeDadoFiltroQuandoListarEntaoConsultaRepositorioComSpecification() {
        List<AgendamentoEntity> agendamentos = List
                .of(agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO));
        when(repository.findAll(ArgumentMatchers.<Specification<AgendamentoEntity>>any())).thenReturn(agendamentos);

        List<AgendamentoEntity> resultado = service
                .findAll(new AgendamentoFilter(AgendamentoStatus.AGENDADO, null, null, null));

        assertSame(agendamentos, resultado);
    }

    @Test
    @DisplayName("Busca agendamento existente por id")
    void testeDadoAgendamentoExistenteQuandoBuscarPorIdEntaoRetornaAgendamento() {
        AgendamentoEntity entity = agendamento(1L, servico(1L, 30), AgendamentoStatus.AGENDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertSame(entity, service.findById(1L));
    }

    @Test
    @DisplayName("Lança exceção ao buscar agendamento inexistente por id")
    void testeDadoAgendamentoInexistenteQuandoBuscarPorIdEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> service.findById(99L));

        assertEquals("Agendamento não foi encontrado", excecao.getMessage());
    }

    private AgendamentoEntity prepararCriacao(AgendamentoCreateDTO request) {
        UsuarioEntity usuario = usuario();
        ServicoEntity servico = servico(1L, 30);
        AgendamentoEntity entity = agendamento(null, servico, AgendamentoStatus.AGENDADO);
        when(usuarioService.findById(2L)).thenReturn(usuario);
        when(servicoService.findById(1L)).thenReturn(servico);
        when(mapper.toEntity(request, usuario, servico)).thenReturn(entity);
        return entity;
    }

    private static AgendamentoCreateDTO requisicaoDeCriacao() {
        return new AgendamentoCreateDTO(INICIO.toLocalDate(), INICIO.toLocalTime(), 2L, 1L);
    }

    private static UsuarioEntity usuario() {
        return UsuarioEntity.builder().id(2L).build();
    }

    private static ServicoEntity servico(Long id, int duracao) {
        return ServicoEntity.builder().id(id).duracao(duracao).build();
    }

    private static AgendamentoEntity agendamento(Long id, ServicoEntity servico, AgendamentoStatus status) {
        return AgendamentoEntity.builder()
                .id(id)
                .data(INICIO)
                .duracao(servico.getDuracao())
                .status(status)
                .usuario(usuario())
                .servico(servico)
                .build();
    }
}
