package org.trimly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.DisponibilidadeEntity;
import org.trimly.backend.model.entity.enums.DiaSemana;
import org.trimly.backend.model.repository.DisponibilidadeRepository;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeValidator;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTests {
    @Mock
    private DisponibilidadeRepository repository;

    @Mock
    private DisponibilidadeMapper mapper;

    @Mock
    private DisponibilidadeValidator disponibilidadeValidator;

    @InjectMocks
    private DisponibilidadeService service;

    /**
     * Verifica que a criação delega a validação dos horários e persiste a entidade convertida pelo mapper.
     */
    @Test
    void disponibilidade_TestCreateDelegaParaValidatorEPersiste() {
        DisponibilidadeCreateDTO request =
                new DisponibilidadeCreateDTO(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0));
        DisponibilidadeEntity mapeada = new DisponibilidadeEntity();
        mapeada.setDiaSemana(DiaSemana.SEGUNDA);
        mapeada.setHoraInicio(LocalTime.of(9, 0));
        mapeada.setHoraFim(LocalTime.of(18, 0));
        DisponibilidadeEntity salva = new DisponibilidadeEntity();
        salva.setId(1L);

        when(mapper.toEntity(request)).thenReturn(mapeada);
        when(repository.save(mapeada)).thenReturn(salva);

        DisponibilidadeEntity resultado = service.create(request);

        verify(disponibilidadeValidator).validateHorarios(LocalTime.of(9, 0), LocalTime.of(18, 0));
        verify(repository).save(mapeada);
        assertThat(resultado).isEqualTo(salva);
    }

    /**
     * Verifica que uma atualização com todos os campos nulos é um no-op: retorna a entidade inalterada sem
     * escrever no repositório.
     */
    @Test
    void disponibilidade_TestUpdateComDtoTodoNuloNaoChamaRepository() {
        DisponibilidadeEntity existente = new DisponibilidadeEntity();
        existente.setId(1L);
        existente.setDiaSemana(DiaSemana.SEGUNDA);
        existente.setHoraInicio(LocalTime.of(9, 0));
        existente.setHoraFim(LocalTime.of(18, 0));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        DisponibilidadeEntity resultado = service.update(1L, new DisponibilidadeUpdateDTO(null, null, null));

        assertThat(resultado).isEqualTo(existente);
        verify(repository, never()).save(any());
        verify(disponibilidadeValidator, never()).validateHorarios(any(), any());
    }

    /**
     * Verifica que uma atualização com apenas alguns campos aplica só esses campos e revalida os horários
     * resultantes.
     */
    @Test
    void disponibilidade_TestUpdateAplicaCamposParciaisERevalida() {
        DisponibilidadeEntity existente = new DisponibilidadeEntity();
        existente.setId(1L);
        existente.setDiaSemana(DiaSemana.SEGUNDA);
        existente.setHoraInicio(LocalTime.of(9, 0));
        existente.setHoraFim(LocalTime.of(18, 0));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        DisponibilidadeEntity resultado =
                service.update(1L, new DisponibilidadeUpdateDTO(null, LocalTime.of(10, 0), null));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(10, 0));
        assertThat(resultado.getHoraFim()).isEqualTo(LocalTime.of(18, 0));
        assertThat(resultado.getDiaSemana()).isEqualTo(DiaSemana.SEGUNDA);
        verify(disponibilidadeValidator).validateHorarios(LocalTime.of(10, 0), LocalTime.of(18, 0));
        verify(repository).save(existente);
    }
}
