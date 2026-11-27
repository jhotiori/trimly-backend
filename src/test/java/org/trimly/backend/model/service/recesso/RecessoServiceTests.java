package org.trimly.backend.model.service.recesso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNaoEncontradoException;
import org.trimly.backend.model.exception.recesso.RecessoNoPassadoException;
import org.trimly.backend.model.exception.recesso.RecessoPeriodoInvalidoException;
import org.trimly.backend.model.repository.RecessoRepository;
import org.trimly.backend.view.dto.recesso.RecessoCreateDTO;
import org.trimly.backend.view.dto.recesso.RecessoUpdateDTO;
import org.trimly.backend.view.mapper.RecessoMapper;

/**
 * Testes para {@link RecessoService}.
 */
@ExtendWith(MockitoExtension.class)
class RecessoServiceTests {
    /**
     * Primeiro dia do recesso usado nos cenários.
     */
    private static final LocalDate INICIO = LocalDate.of(2030, 12, 1);

    /**
     * Último dia do recesso usado nos cenários.
     */
    private static final LocalDate FIM = LocalDate.of(2030, 12, 17);

    /**
     * Repositório de recessos simulado.
     * @see {@link RecessoRepository}
     */
    @Mock
    private RecessoRepository repository;

    /**
     * Mapper de recessos simulado.
     * @see {@link RecessoMapper}
     */
    @Mock
    private RecessoMapper mapper;

    /**
     * Validador de recessos simulado.
     * @see {@link RecessoValidator}
     */
    @Mock
    private RecessoValidator recessoValidator;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link RecessoService}
     */
    @InjectMocks
    private RecessoService service;

    @Test
    @DisplayName("Valida passado, período e conflito, nessa ordem, antes de salvar")
    void testeDadoRecessoValidoQuandoCriarEntaoValidaNaOrdemESalva() {
        RecessoCreateDTO request = new RecessoCreateDTO("Férias", INICIO, FIM);
        RecessoEntity entity = recesso(null);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        RecessoEntity resultado = service.create(request);

        assertSame(entity, resultado);
        InOrder ordem = inOrder(recessoValidator);
        ordem.verify(recessoValidator).validateNoPassado(INICIO);
        ordem.verify(recessoValidator).validatePeriodo(INICIO, FIM);
        ordem.verify(recessoValidator).validateConflito(null, INICIO, FIM);
    }

    @Test
    @DisplayName("Não valida o resto nem salva quando o início está no passado")
    void testeDadoInicioNoPassadoQuandoCriarEntaoLancaExcecaoSemSalvar() {
        RecessoCreateDTO request = new RecessoCreateDTO("Férias", INICIO, FIM);
        when(mapper.toEntity(request)).thenReturn(recesso(null));
        doThrow(new RecessoNoPassadoException("Passado")).when(recessoValidator).validateNoPassado(INICIO);

        assertThrows(RecessoNoPassadoException.class, () -> service.create(request));

        verify(recessoValidator, never()).validatePeriodo(any(), any());
        verify(recessoValidator, never()).validateConflito(any(), any(), any());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Não checa conflito nem salva quando o fim é anterior ao início")
    void testeDadoPeriodoInvalidoQuandoCriarEntaoLancaExcecaoSemSalvar() {
        RecessoCreateDTO request = new RecessoCreateDTO("Férias", INICIO, FIM);
        when(mapper.toEntity(request)).thenReturn(recesso(null));
        doThrow(new RecessoPeriodoInvalidoException("Período")).when(recessoValidator).validatePeriodo(INICIO, FIM);

        assertThrows(RecessoPeriodoInvalidoException.class, () -> service.create(request));

        verify(recessoValidator, never()).validateConflito(any(), any(), any());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Não salva quando há conflito com outro recesso")
    void testeDadoConflitoQuandoCriarEntaoLancaExcecaoSemSalvar() {
        RecessoCreateDTO request = new RecessoCreateDTO("Férias", INICIO, FIM);
        when(mapper.toEntity(request)).thenReturn(recesso(null));
        doThrow(new RecessoConflitoException("Conflito")).when(recessoValidator).validateConflito(null, INICIO, FIM);

        assertThrows(RecessoConflitoException.class, () -> service.create(request));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Atualiza descrição e datas informadas, valida só as datas enviadas contra o passado e revalida")
    void testeDadoTodosOsCamposQuandoAtualizarEntaoAplicaAlteracoesERevalida() {
        RecessoEntity entity = recesso(1L);
        LocalDate novoInicio = LocalDate.of(2030, 12, 5);
        LocalDate novoFim = LocalDate.of(2030, 12, 25);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        RecessoEntity resultado = service.update(1L, new RecessoUpdateDTO("Reforma", novoInicio, novoFim));

        assertSame(entity, resultado);
        assertEquals("Reforma", entity.getDescricao());
        assertEquals(novoInicio, entity.getDataInicio());
        assertEquals(novoFim, entity.getDataFim());
        verify(recessoValidator).validateNoPassado(novoInicio);
        verify(recessoValidator).validateNoPassado(novoFim);
        verify(recessoValidator).validatePeriodo(novoInicio, novoFim);
        verify(recessoValidator).validateConflito(1L, novoInicio, novoFim);
    }

    @Test
    @DisplayName("Atualiza só a descrição sem checar as datas existentes contra o passado")
    void testeDadoSoDescricaoQuandoAtualizarEntaoNaoValidaPassado() {
        RecessoEntity entity = recesso(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        service.update(1L, new RecessoUpdateDTO("Reforma", null, null));

        assertEquals("Reforma", entity.getDescricao());
        assertEquals(INICIO, entity.getDataInicio());
        assertEquals(FIM, entity.getDataFim());
        verify(recessoValidator, never()).validateNoPassado(any());
        verify(recessoValidator).validatePeriodo(INICIO, FIM);
        verify(recessoValidator).validateConflito(1L, INICIO, FIM);
    }

    @Test
    @DisplayName("Rejeita data de fim enviada no passado sem salvar")
    void testeDadoFimNoPassadoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        when(repository.findById(1L)).thenReturn(Optional.of(recesso(1L)));
        doThrow(new RecessoNoPassadoException("Passado")).when(recessoValidator).validateNoPassado(ontem);

        assertThrows(RecessoNoPassadoException.class, () -> service.update(1L, new RecessoUpdateDTO(null, null, ontem)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando o novo período conflita com outro recesso")
    void testeDadoConflitoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        LocalDate novoFim = LocalDate.of(2030, 12, 25);
        when(repository.findById(1L)).thenReturn(Optional.of(recesso(1L)));
        doThrow(new RecessoConflitoException("Conflito")).when(recessoValidator).validateConflito(1L, INICIO, novoFim);

        assertThrows(RecessoConflitoException.class, () -> service.update(1L, new RecessoUpdateDTO(null, null, novoFim)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Retorna o recesso inalterado quando nenhum campo é informado")
    void testeDadoTodosNulosQuandoAtualizarEntaoRetornaInalteradoSemEscritaNemValidacao() {
        RecessoEntity entity = recesso(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        RecessoEntity resultado = service.update(1L, new RecessoUpdateDTO(null, null, null));

        assertSame(entity, resultado);
        verifyNoInteractions(recessoValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Lança não encontrado ao atualizar recesso inexistente")
    void testeDadoIdInexistenteQuandoAtualizarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecessoNaoEncontradoException.class, () -> service.update(99L, new RecessoUpdateDTO("X", null, null)));
    }

    @Test
    @DisplayName("Remove o recesso encontrado")
    void testeDadoRecessoExistenteQuandoDeletarEntaoRemove() {
        RecessoEntity entity = recesso(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteById(1L);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Lança não encontrado ao remover recesso inexistente")
    void testeDadoIdInexistenteQuandoDeletarEntaoLancaNaoEncontradoSemRemover() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecessoNaoEncontradoException.class, () -> service.deleteById(99L));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Lista todos os recessos")
    void testeDadoRecessosCadastradosQuandoListarEntaoRetornaTodos() {
        List<RecessoEntity> recessos = List.of(recesso(1L), recesso(2L));
        when(repository.findAll()).thenReturn(recessos);

        assertSame(recessos, service.findAll());
    }

    @Test
    @DisplayName("Busca o recesso pelo id")
    void testeDadoIdExistenteQuandoBuscarPorIdEntaoRetornaORecesso() {
        RecessoEntity entity = recesso(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertSame(entity, service.findById(1L));
    }

    @Test
    @DisplayName("Lança não encontrado ao buscar id inexistente")
    void testeDadoIdInexistenteQuandoBuscarPorIdEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecessoNaoEncontradoException.class, () -> service.findById(99L));
    }

    private static RecessoEntity recesso(Long id) {
        return RecessoEntity.builder().id(id).descricao("Férias").dataInicio(INICIO).dataFim(FIM).build();
    }
}
