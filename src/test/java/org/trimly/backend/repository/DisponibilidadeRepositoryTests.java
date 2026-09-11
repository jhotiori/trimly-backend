package org.trimly.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.trimly.backend.model.entity.DisponibilidadeEntity;
import org.trimly.backend.model.entity.enums.DiaSemana;
import org.trimly.backend.model.repository.DisponibilidadeRepository;

@RepositoryTest
class DisponibilidadeRepositoryTests {
    @Autowired
    private DisponibilidadeRepository repository;

    /**
     * Verifica que {@code findByDiaSemana} retorna apenas as disponibilidades cadastradas para o dia informado,
     * ignorando as de outros dias.
     */
    @Test
    void disponibilidade_TestFindByDiaSemanaRetornaSomenteODiaInformado() {
        repository.saveAndFlush(criarDisponibilidade(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        repository.saveAndFlush(criarDisponibilidade(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(21, 0)));
        repository.saveAndFlush(criarDisponibilidade(DiaSemana.TERCA, LocalTime.of(9, 0), LocalTime.of(18, 0)));

        List<DisponibilidadeEntity> resultado = repository.findByDiaSemana(DiaSemana.SEGUNDA);

        assertThat(resultado).hasSize(2).allMatch(d -> d.getDiaSemana() == DiaSemana.SEGUNDA);
    }

    /**
     * Verifica que {@code findByDiaSemana} retorna uma lista vazia para um dia sem nenhuma disponibilidade
     * cadastrada.
     */
    @Test
    void disponibilidade_TestFindByDiaSemanaSemNenhumaCadastradaRetornaListaVazia() {
        repository.saveAndFlush(criarDisponibilidade(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0)));

        List<DisponibilidadeEntity> resultado = repository.findByDiaSemana(DiaSemana.DOMINGO);

        assertThat(resultado).isEmpty();
    }

    private DisponibilidadeEntity criarDisponibilidade(DiaSemana diaSemana, LocalTime inicio, LocalTime fim) {
        DisponibilidadeEntity entity = new DisponibilidadeEntity();
        entity.setDiaSemana(diaSemana);
        entity.setHoraInicio(inicio);
        entity.setHoraFim(fim);
        return entity;
    }
}
