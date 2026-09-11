package org.trimly.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.enums.ServicoStatus;
import org.trimly.backend.model.repository.ServicoRepository;

@RepositoryTest
class ServicoRepositoryTests {
    @Autowired
    private ServicoRepository repository;

    /**
     * Verifica que {@code findByNomeLikeIgnoreCase} encontra o serviço independentemente de maiúsculas e
     * minúsculas no padrão de busca.
     */
    @Test
    void servico_TestFindByNomeLikeIgnoreCaseEncontraIgnorandoCaixa() {
        repository.saveAndFlush(criarServico("Corte Masculino", ServicoStatus.ATIVO));

        List<ServicoEntity> resultado = repository.findByNomeLikeIgnoreCase("%corte%");

        assertThat(resultado).extracting(ServicoEntity::getNome).containsExactly("Corte Masculino");
    }

    /**
     * Verifica que {@code existsByNomeIgnoreCase} retorna {@code true} para um nome já cadastrado,
     * independentemente da caixa.
     */
    @Test
    void servico_TestExistsByNomeIgnoreCaseComNomeExistenteRetornaTrue() {
        repository.saveAndFlush(criarServico("Corte Masculino", ServicoStatus.ATIVO));

        assertThat(repository.existsByNomeIgnoreCase("corte masculino")).isTrue();
    }

    /**
     * Verifica que {@code existsByNomeIgnoreCase} retorna {@code false} para um nome não cadastrado.
     */
    @Test
    void servico_TestExistsByNomeIgnoreCaseComNomeInexistenteRetornaFalse() {
        repository.saveAndFlush(criarServico("Corte Masculino", ServicoStatus.ATIVO));

        assertThat(repository.existsByNomeIgnoreCase("Barba")).isFalse();
    }

    /**
     * Verifica que {@code findByStatus} retorna somente os serviços com o status informado. O método original,
     * {@code findByStatusEqualsIgnoreCase(String)}, foi corrigido para {@code findByStatus(ServicoStatus)}: a
     * variante {@code IgnoreCase} não é aplicável a uma propriedade enumerada e lançava
     * {@code InvalidDataAccessApiUsageException} em toda chamada.
     */
    @Test
    void servico_TestFindByStatusRetornaSomenteOStatusInformado() {
        repository.saveAndFlush(criarServico("Corte Masculino", ServicoStatus.ATIVO));
        repository.saveAndFlush(criarServico("Barba", ServicoStatus.INATIVO));

        List<ServicoEntity> resultado = repository.findByStatus(ServicoStatus.ATIVO);

        assertThat(resultado).extracting(ServicoEntity::getNome).containsExactly("Corte Masculino");
    }

    private ServicoEntity criarServico(String nome, ServicoStatus status) {
        ServicoEntity entity = new ServicoEntity();
        entity.setNome(nome);
        entity.setValor(BigDecimal.TEN);
        entity.setDuracao(30);
        entity.setStatus(status);
        return entity;
    }
}
