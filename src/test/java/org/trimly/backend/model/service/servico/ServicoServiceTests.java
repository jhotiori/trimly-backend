package org.trimly.backend.model.service.servico;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.repository.ServicoRepository;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

/**
 * Testes para {@link ServicoService}.
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTests {
    /**
     * Repositório de serviços simulado.
     * @see {@link ServicoRepository}
     */
    @Mock
    private ServicoRepository repository;

    /**
     * Mapper de serviços simulado.
     * @see {@link ServicoMapper}
     */
    @Mock
    private ServicoMapper mapper;

    /**
     * Validador de serviços simulado.
     * @see {@link ServicoValidator}
     */
    @Mock
    private ServicoValidator servicoValidator;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link ServicoService}
     */
    @InjectMocks
    private ServicoService service;

    @Test
    @DisplayName("Valida o nome e salva o serviço criado")
    void testeDadoNomeUnicoQuandoCriarEntaoSalvaServico() {
        ServicoCreateDTO request = new ServicoCreateDTO("Corte", new BigDecimal("40.00"), 30);
        ServicoEntity entity = servico(null, "Corte");
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        ServicoEntity resultado = service.create(request);

        assertSame(entity, resultado);
        verify(servicoValidator).validateNomeUnico("Corte");
    }

    @Test
    @DisplayName("Não salva quando o nome já existe")
    void testeDadoNomeDuplicadoQuandoCriarEntaoLancaExcecaoSemSalvar() {
        ServicoCreateDTO request = new ServicoCreateDTO("Corte", new BigDecimal("40.00"), 30);
        doThrow(new ServicoNomeDuplicadoException("Já existe um serviço com esse nome")).when(servicoValidator)
                .validateNomeUnico("Corte");

        assertThrows(ServicoNomeDuplicadoException.class, () -> service.create(request));

        verifyNoInteractions(mapper, repository);
    }

    @Test
    @DisplayName("Atualiza todos os campos informados")
    void testeDadoTodosOsCamposQuandoAtualizarEntaoAplicaAlteracoes() {
        ServicoEntity entity = servico(1L, "Corte");
        ServicoUpdateDTO request = new ServicoUpdateDTO("Barba", new BigDecimal("55.00"), 45, ServicoStatus.INATIVO);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        ServicoEntity resultado = service.update(1L, request);

        assertAll(
                () -> assertEquals("Barba", resultado.getNome()),
                () -> assertEquals(new BigDecimal("55.00"), resultado.getValor()),
                () -> assertEquals(45, resultado.getDuracao()),
                () -> assertEquals(ServicoStatus.INATIVO, resultado.getStatus())
        );
        verify(servicoValidator).validateStatusUpdate(entity, ServicoStatus.INATIVO);
    }

    @Test
    @DisplayName("Ignora nome em branco, valor e duração não positivos")
    void testeDadoValoresInvalidosQuandoAtualizarEntaoMantemCamposOriginais() {
        ServicoEntity entity = servico(1L, "Corte");
        ServicoUpdateDTO request = new ServicoUpdateDTO(" ", BigDecimal.ZERO, 0, null);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        ServicoEntity resultado = service.update(1L, request);

        assertAll(
                () -> assertEquals("Corte", resultado.getNome()),
                () -> assertEquals(new BigDecimal("40.00"), resultado.getValor()),
                () -> assertEquals(30, resultado.getDuracao()),
                () -> assertEquals(ServicoStatus.ATIVO, resultado.getStatus())
        );
    }

    @Test
    @DisplayName("Não escreve nem valida quando todos os campos são nulos")
    void testeDadoTodosOsCamposNulosQuandoAtualizarEntaoRetornaSemAlterar() {
        ServicoEntity entity = servico(1L, "Corte");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        ServicoEntity resultado = service.update(1L, new ServicoUpdateDTO(null, null, null, null));

        assertSame(entity, resultado);
        verifyNoInteractions(servicoValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando o status não é permitido")
    void testeDadoStatusInvalidoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        ServicoEntity entity = servico(1L, "Corte");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new ServicoException("O serviço já está nesse status")).when(servicoValidator)
                .validateStatusUpdate(entity, ServicoStatus.ATIVO);

        ServicoUpdateDTO request = new ServicoUpdateDTO(null, null, null, ServicoStatus.ATIVO);

        assertThrows(ServicoException.class, () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Lança exceção ao atualizar serviço inexistente")
    void testeDadoServicoInexistenteQuandoAtualizarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ServicoUpdateDTO request = new ServicoUpdateDTO("Barba", null, null, null);

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
    }

    @Test
    @DisplayName("Remove serviço sem agendamento pendente")
    void testeDadoServicoSemAgendamentoPendenteQuandoDeletarEntaoRemove() {
        ServicoEntity entity = servico(1L, "Corte");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteById(1L);

        verify(servicoValidator).validateSemAgendamentoPendente(1L);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Não remove serviço com agendamento pendente")
    void testeDadoServicoComAgendamentoPendenteQuandoDeletarEntaoLancaExcecaoSemRemover() {
        when(repository.findById(1L)).thenReturn(Optional.of(servico(1L, "Corte")));
        doThrow(new ServicoComAgendamentoPendenteException("Serviço com agendamento pendente")).when(servicoValidator)
                .validateSemAgendamentoPendente(1L);

        assertThrows(ServicoComAgendamentoPendenteException.class, () -> service.deleteById(1L));

        verify(repository, never()).delete(any(ServicoEntity.class));
    }

    @Test
    @DisplayName("Lança exceção ao remover serviço inexistente")
    void testeDadoServicoInexistenteQuandoDeletarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));

        verifyNoInteractions(servicoValidator);
    }

    @Test
    @DisplayName("Lista todos os serviços do repositório")
    void testeDadoServicosCadastradosQuandoListarEntaoRetornaTodos() {
        List<ServicoEntity> servicos = List.of(servico(1L, "Corte"), servico(2L, "Barba"));
        when(repository.findAll()).thenReturn(servicos);

        assertSame(servicos, service.findAll());
    }

    @Test
    @DisplayName("Busca serviço existente por id")
    void testeDadoServicoExistenteQuandoBuscarPorIdEntaoRetornaServico() {
        ServicoEntity entity = servico(1L, "Corte");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertSame(entity, service.findById(1L));
    }

    @Test
    @DisplayName("Lança exceção ao buscar serviço inexistente por id")
    void testeDadoServicoInexistenteQuandoBuscarPorIdEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> service.findById(99L));

        assertEquals("Serviço não encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("Lista serviços pelo status informado")
    void testeDadoStatusQuandoBuscarPorStatusEntaoRetornaServicosDoStatus() {
        List<ServicoEntity> ativos = List.of(servico(1L, "Corte"));
        when(repository.findByStatus(ServicoStatus.ATIVO)).thenReturn(ativos);

        assertSame(ativos, service.findByStatus(ServicoStatus.ATIVO));
    }

    private static ServicoEntity servico(Long id, String nome) {
        return ServicoEntity.builder()
                .id(id)
                .nome(nome)
                .valor(new BigDecimal("40.00"))
                .duracao(30)
                .status(ServicoStatus.ATIVO)
                .build();
    }
}
