package org.trimly.backend.model.service.disponibilidade;

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
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;
import org.trimly.backend.model.repository.DisponibilidadeRepository;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

/**
 * Testes para {@link DisponibilidadeService}.
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTests {
    /**
     * Hora de início da janela usada nos cenários.
     */
    private static final LocalTime INICIO = LocalTime.of(16, 0);

    /**
     * Hora de fim da janela usada nos cenários.
     */
    private static final LocalTime FIM = LocalTime.of(18, 0);

    /**
     * Repositório de disponibilidades simulado.
     * @see {@link DisponibilidadeRepository}
     */
    @Mock
    private DisponibilidadeRepository repository;

    /**
     * Mapper de disponibilidades simulado.
     * @see {@link DisponibilidadeMapper}
     */
    @Mock
    private DisponibilidadeMapper mapper;

    /**
     * Validador de disponibilidades simulado.
     * @see {@link DisponibilidadeValidator}
     */
    @Mock
    private DisponibilidadeValidator disponibilidadeValidator;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link DisponibilidadeService}
     */
    @InjectMocks
    private DisponibilidadeService service;

    @Test
    @DisplayName("Valida horários e conflito, nessa ordem, antes de salvar")
    void testeDadoHorarioValidoQuandoCriarEntaoValidaNaOrdemESalva() {
        DisponibilidadeCreateDTO request = new DisponibilidadeCreateDTO(DiaSemana.SEXTA, INICIO, FIM);
        DisponibilidadeEntity entity = disponibilidade(null, DiaSemana.SEXTA);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        DisponibilidadeEntity resultado = service.create(request);

        assertSame(entity, resultado);
        InOrder ordem = inOrder(disponibilidadeValidator);
        ordem.verify(disponibilidadeValidator).validateHorarios(INICIO, FIM);
        ordem.verify(disponibilidadeValidator).validateConflitoDeHorario(null, DiaSemana.SEXTA, INICIO, FIM);
    }

    @Test
    @DisplayName("Não salva nem checa conflito quando o horário é inválido")
    void testeDadoHorarioInvalidoQuandoCriarEntaoLancaExcecaoSemSalvar() {
        DisponibilidadeCreateDTO request = new DisponibilidadeCreateDTO(DiaSemana.SEXTA, INICIO, FIM);
        when(mapper.toEntity(request)).thenReturn(disponibilidade(null, DiaSemana.SEXTA));
        doThrow(new DisponibilidadeHorarioInvalidoException("Horário inválido")).when(disponibilidadeValidator)
                .validateHorarios(INICIO, FIM);

        assertThrows(DisponibilidadeHorarioInvalidoException.class, () -> service.create(request));

        verify(disponibilidadeValidator, never()).validateConflitoDeHorario(any(), any(), any(), any());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Não salva quando há conflito de horário")
    void testeDadoConflitoDeHorarioQuandoCriarEntaoLancaExcecaoSemSalvar() {
        DisponibilidadeCreateDTO request = new DisponibilidadeCreateDTO(DiaSemana.SEXTA, INICIO, FIM);
        when(mapper.toEntity(request)).thenReturn(disponibilidade(null, DiaSemana.SEXTA));
        doThrow(new DisponibilidadeConflitoException("Horário ocupado")).when(disponibilidadeValidator)
                .validateConflitoDeHorario(null, DiaSemana.SEXTA, INICIO, FIM);

        assertThrows(DisponibilidadeConflitoException.class, () -> service.create(request));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Atualiza dia e horários informados e revalida")
    void testeDadoTodosOsCamposQuandoAtualizarEntaoAplicaAlteracoesERevalida() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        LocalTime novoInicio = LocalTime.of(8, 0);
        LocalTime novoFim = LocalTime.of(12, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        DisponibilidadeEntity resultado = service
                .update(1L, new DisponibilidadeUpdateDTO(DiaSemana.SABADO, novoInicio, novoFim));

        assertAll(
                () -> assertEquals(DiaSemana.SABADO, resultado.getDiaSemana()),
                () -> assertEquals(novoInicio, resultado.getHoraInicio()),
                () -> assertEquals(novoFim, resultado.getHoraFim())
        );
        InOrder ordem = inOrder(disponibilidadeValidator);
        ordem.verify(disponibilidadeValidator).validateHorarios(novoInicio, novoFim);
        ordem.verify(disponibilidadeValidator).validateConflitoDeHorario(1L, DiaSemana.SABADO, novoInicio, novoFim);
    }

    @Test
    @DisplayName("Mantém os campos não informados ao atualizar")
    void testeDadoApenasHoraFimQuandoAtualizarEntaoMantemDemaisCampos() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        LocalTime novoFim = LocalTime.of(19, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        DisponibilidadeEntity resultado = service.update(1L, new DisponibilidadeUpdateDTO(null, null, novoFim));

        assertAll(
                () -> assertEquals(DiaSemana.SEXTA, resultado.getDiaSemana()),
                () -> assertEquals(INICIO, resultado.getHoraInicio()),
                () -> assertEquals(novoFim, resultado.getHoraFim())
        );
        verify(disponibilidadeValidator).validateHorarios(INICIO, novoFim);
    }

    @Test
    @DisplayName("Não escreve nem valida quando todos os campos são nulos")
    void testeDadoTodosOsCamposNulosQuandoAtualizarEntaoRetornaSemAlterar() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        DisponibilidadeEntity resultado = service.update(1L, new DisponibilidadeUpdateDTO(null, null, null));

        assertSame(entity, resultado);
        verifyNoInteractions(disponibilidadeValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando o novo horário é inválido")
    void testeDadoHorarioInvalidoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        LocalTime novoInicio = LocalTime.of(19, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new DisponibilidadeHorarioInvalidoException("Horário inválido")).when(disponibilidadeValidator)
                .validateHorarios(novoInicio, FIM);

        DisponibilidadeUpdateDTO request = new DisponibilidadeUpdateDTO(null, novoInicio, null);

        assertThrows(DisponibilidadeHorarioInvalidoException.class, () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Lança exceção ao atualizar disponibilidade inexistente")
    void testeDadoDisponibilidadeInexistenteQuandoAtualizarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        DisponibilidadeUpdateDTO request = new DisponibilidadeUpdateDTO(DiaSemana.SABADO, null, null);

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
    }

    @Test
    @DisplayName("Remove disponibilidade existente")
    void testeDadoDisponibilidadeExistenteQuandoDeletarEntaoRemove() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteById(1L);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Lança exceção ao remover disponibilidade inexistente")
    void testeDadoDisponibilidadeInexistenteQuandoDeletarEntaoLancaNaoEncontradoSemRemover() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));

        verify(repository, never()).delete(any(DisponibilidadeEntity.class));
    }

    @Test
    @DisplayName("Lista todas as disponibilidades do repositório")
    void testeDadoDisponibilidadesCadastradasQuandoListarEntaoRetornaTodas() {
        List<DisponibilidadeEntity> disponibilidades = List.of(disponibilidade(1L, DiaSemana.SEXTA));
        when(repository.findAll()).thenReturn(disponibilidades);

        assertSame(disponibilidades, service.findAll());
    }

    @Test
    @DisplayName("Busca disponibilidade existente por id")
    void testeDadoDisponibilidadeExistenteQuandoBuscarPorIdEntaoRetornaDisponibilidade() {
        DisponibilidadeEntity entity = disponibilidade(1L, DiaSemana.SEXTA);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertSame(entity, service.findById(1L));
    }

    @Test
    @DisplayName("Lança exceção ao buscar disponibilidade inexistente por id")
    void testeDadoDisponibilidadeInexistenteQuandoBuscarPorIdEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> service.findById(99L));

        assertEquals("Disponibilidade não foi encontrada", excecao.getMessage());
    }

    @Test
    @DisplayName("Lista disponibilidades do dia da semana informado")
    void testeDadoDiaDaSemanaQuandoBuscarPorDiaEntaoRetornaDisponibilidadesDoDia() {
        List<DisponibilidadeEntity> sextas = List.of(disponibilidade(1L, DiaSemana.SEXTA));
        when(repository.findByDiaSemana(DiaSemana.SEXTA)).thenReturn(sextas);

        assertSame(sextas, service.findByDiaSemana(DiaSemana.SEXTA));
    }

    private static DisponibilidadeEntity disponibilidade(Long id, DiaSemana diaSemana) {
        return DisponibilidadeEntity.builder().id(id).diaSemana(diaSemana).horaInicio(INICIO).horaFim(FIM).build();
    }
}
