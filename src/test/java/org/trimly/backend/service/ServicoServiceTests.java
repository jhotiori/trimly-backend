package org.trimly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.enums.ServicoStatus;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.repository.ServicoRepository;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.model.service.servico.ServicoValidator;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTests {
    @Mock
    private ServicoRepository repository;

    @Mock
    private ServicoMapper mapper;

    @Mock
    private ServicoValidator servicoValidator;

    @InjectMocks
    private ServicoService service;

    /**
     * Verifica que a criação define o status como {@code ATIVO} e valida a unicidade do nome antes de persistir.
     */
    @Test
    void servico_TestCreateDefineStatusAtivoEValidaNomeUnico() {
        ServicoCreateDTO request = new ServicoCreateDTO("Corte", BigDecimal.TEN, 30);
        ServicoEntity mapeada = new ServicoEntity();
        mapeada.setNome("Corte");

        when(mapper.toEntity(request)).thenReturn(mapeada);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoEntity resultado = service.create(request);

        ArgumentCaptor<ServicoEntity> captor = ArgumentCaptor.forClass(ServicoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ServicoStatus.ATIVO);
        verify(servicoValidator).validateNomeUnico("Corte");
        assertThat(resultado.getStatus()).isEqualTo(ServicoStatus.ATIVO);
    }

    /**
     * Verifica que uma atualização com todos os campos nulos é um no-op: retorna a entidade inalterada sem
     * escrever no repositório.
     */
    @Test
    void servico_TestUpdateComDtoTodoNuloNaoChamaRepository() {
        ServicoEntity existente = new ServicoEntity();
        existente.setId(1L);
        existente.setNome("Corte");
        existente.setStatus(ServicoStatus.ATIVO);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        ServicoEntity resultado = service.update(1L, new ServicoUpdateDTO(null, null, null, null));

        assertThat(resultado).isEqualTo(existente);
        verify(repository, never()).save(any());
    }

    /**
     * Verifica que uma atualização só aplica os campos não nulos e positivos, ignorando valor e duração
     * inválidos.
     */
    @Test
    void servico_TestUpdateAplicaSomenteCamposNaoNulosEPositivos() {
        ServicoEntity existente = new ServicoEntity();
        existente.setId(1L);
        existente.setNome("Corte");
        existente.setValor(BigDecimal.TEN);
        existente.setDuracao(30);
        existente.setStatus(ServicoStatus.ATIVO);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ServicoEntity resultado =
                service.update(1L, new ServicoUpdateDTO("Corte Novo", BigDecimal.valueOf(-5), 0, null));

        assertThat(resultado.getNome()).isEqualTo("Corte Novo");
        assertThat(resultado.getValor()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(resultado.getDuracao()).isEqualTo(30);
        verify(repository).save(existente);
    }

    /**
     * Verifica que uma exceção lançada pelo validator durante a atualização é propagada sem persistir a entidade.
     */
    @Test
    void servico_TestUpdatePropagaExcecaoDoValidatorSemSalvar() {
        ServicoEntity existente = new ServicoEntity();
        existente.setId(1L);
        existente.setStatus(ServicoStatus.ATIVO);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new ServicoException("O serviço já está nesse status"))
                .when(servicoValidator)
                .validateStatusUpdate(existente, ServicoStatus.ATIVO);

        assertThatThrownBy(() -> service.update(1L, new ServicoUpdateDTO(null, null, null, ServicoStatus.ATIVO)))
                .isInstanceOf(ServicoException.class);
        verify(repository, never()).save(any());
    }

    /**
     * Verifica que a remoção propaga {@link ServicoComAgendamentoPendenteException} sem chegar a remover o
     * serviço do repositório.
     */
    @Test
    void servico_TestDeleteByIdPropagaExcecaoAntesDeRemover() {
        ServicoEntity existente = new ServicoEntity();
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new ServicoComAgendamentoPendenteException(
                        "Não é possível remover um serviço com agendamento pendente"))
                .when(servicoValidator)
                .validateSemAgendamentoPendente(1L);

        assertThatThrownBy(() -> service.deleteById(1L)).isInstanceOf(ServicoComAgendamentoPendenteException.class);
        verify(repository, never()).deleteById(any());
    }
}
