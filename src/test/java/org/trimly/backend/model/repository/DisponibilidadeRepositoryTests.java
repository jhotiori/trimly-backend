package org.trimly.backend.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;

/**
 * Testes para {@link DisponibilidadeRepository}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class DisponibilidadeRepositoryTests {
    /**
     * Repositório de disponibilidades sob teste.
     * @see {@link DisponibilidadeRepository}
     */
    @Autowired
    private DisponibilidadeRepository repository;

    @Test
    @DisplayName("Lista apenas as janelas do dia da semana informado")
    void testeDadoJanelasDeDiasDiferentesQuandoBuscarPorDiaEntaoRetornaSoAsDoDia() {
        salvar(DiaSemana.SEGUNDA, 8, 12);
        salvar(DiaSemana.SEGUNDA, 13, 18);
        salvar(DiaSemana.TERCA, 8, 12);

        List<DisponibilidadeEntity> segundas = repository.findByDiaSemana(DiaSemana.SEGUNDA);

        assertThat(segundas).extracting(DisponibilidadeEntity::getHoraInicio)
                .containsExactlyInAnyOrder(LocalTime.of(8, 0), LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("Retorna lista vazia para dia sem janelas")
    void testeDadoDiaSemJanelasQuandoBuscarPorDiaEntaoRetornaListaVazia() {
        salvar(DiaSemana.SEGUNDA, 8, 12);

        assertThat(repository.findByDiaSemana(DiaSemana.DOMINGO)).isEmpty();
    }

    private void salvar(DiaSemana diaSemana, int horaInicio, int horaFim) {
        repository.save(
                DisponibilidadeEntity.builder()
                        .diaSemana(diaSemana)
                        .horaInicio(LocalTime.of(horaInicio, 0))
                        .horaFim(LocalTime.of(horaFim, 0))
                        .build()
        );
    }
}
