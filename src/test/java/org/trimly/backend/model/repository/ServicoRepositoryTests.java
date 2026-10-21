package org.trimly.backend.model.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;

/**
 * Testes para {@link ServicoRepository}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class ServicoRepositoryTests {
    /**
     * Repositório de serviços sob teste.
     * @see {@link ServicoRepository}
     */
    @Autowired
    private ServicoRepository repository;

    @Test
    @DisplayName("Encontra nome existente ignorando maiúsculas e minúsculas")
    void testeDadoNomeComCaixaDiferenteQuandoVerificarExistenciaEntaoRetornaVerdadeiro() {
        salvar("Corte Masculino", ServicoStatus.ATIVO);

        assertTrue(repository.existsByNomeIgnoreCase("corte MASCULINO"));
    }

    @Test
    @DisplayName("Nega nome que não está cadastrado")
    void testeDadoNomeNaoCadastradoQuandoVerificarExistenciaEntaoRetornaFalso() {
        salvar("Corte Masculino", ServicoStatus.ATIVO);

        assertFalse(repository.existsByNomeIgnoreCase("Barba"));
    }

    @Test
    @DisplayName("Lista apenas serviços do status informado")
    void testeDadoServicosDeStatusDiferentesQuandoBuscarPorStatusEntaoRetornaSoOsDoStatus() {
        salvar("Corte", ServicoStatus.ATIVO);
        salvar("Barba", ServicoStatus.ATIVO);
        salvar("Pigmentação", ServicoStatus.INATIVO);

        List<ServicoEntity> ativos = repository.findByStatus(ServicoStatus.ATIVO);

        assertThat(ativos).extracting(ServicoEntity::getNome).containsExactlyInAnyOrder("Corte", "Barba");
    }

    private void salvar(String nome, ServicoStatus status) {
        repository.save(
                ServicoEntity.builder().nome(nome).valor(new BigDecimal("40.00")).duracao(30).status(status).build()
        );
    }
}
