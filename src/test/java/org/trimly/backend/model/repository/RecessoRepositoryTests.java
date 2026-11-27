package org.trimly.backend.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.trimly.backend.model.entity.recesso.RecessoEntity;

/**
 * Testes para {@link RecessoRepository}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class RecessoRepositoryTests {
    /**
     * Primeiro dia do recesso salvo nos cenários.
     */
    private static final LocalDate INICIO = LocalDate.of(2030, 12, 10);

    /**
     * Último dia do recesso salvo nos cenários.
     */
    private static final LocalDate FIM = LocalDate.of(2030, 12, 20);

    /**
     * Repositório de recessos sob teste.
     * @see {@link RecessoRepository}
     */
    @Autowired
    private RecessoRepository repository;

    @Test
    @DisplayName("Encontra o recesso quando a data é o primeiro ou o último dia")
    void testeDadoDataNasPontasQuandoBuscarSobrepostosEntaoRetornaORecesso() {
        salvar(INICIO, FIM);

        assertThat(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(INICIO, INICIO)).hasSize(1);
        assertThat(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM, FIM)).hasSize(1);
    }

    @Test
    @DisplayName("Não encontra o recesso um dia antes do início nem um dia depois do fim")
    void testeDadoDataForaDoRecessoQuandoBuscarSobrepostosEntaoRetornaListaVazia() {
        salvar(INICIO, FIM);

        LocalDate diaAntes = INICIO.minusDays(1);
        LocalDate diaDepois = FIM.plusDays(1);

        assertThat(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(diaAntes, diaAntes)).isEmpty();
        assertThat(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(diaDepois, diaDepois)).isEmpty();
    }

    @Test
    @DisplayName("Encontra recessos que cruzam o período, inclusive os que só encostam nas pontas")
    void testeDadoPeriodoQueCruzaQuandoBuscarSobrepostosEntaoRetornaSoOsSobrepostos() {
        salvar(INICIO, FIM);
        salvar(LocalDate.of(2031, 1, 5), LocalDate.of(2031, 1, 8));

        List<RecessoEntity> encostando = repository
                .findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM.plusDays(5), FIM);

        assertThat(encostando).extracting(RecessoEntity::getDataInicio).containsExactly(INICIO);
    }

    private void salvar(LocalDate dataInicio, LocalDate dataFim) {
        repository.save(RecessoEntity.builder().descricao("Férias").dataInicio(dataInicio).dataFim(dataFim).build());
    }
}
