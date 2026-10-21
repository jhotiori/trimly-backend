package org.trimly.backend.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;

/**
 * Testes para {@link AgendamentoSpecification}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AgendamentoSpecificationTests {
    /**
     * Dia base dos cenários.
     */
    private static final LocalDate DIA = LocalDate.of(2030, 1, 7);

    /**
     * Repositório de agendamentos sob teste.
     * @see {@link AgendamentoRepository}
     */
    @Autowired
    private AgendamentoRepository repository;

    /**
     * Repositório de usuários, usado para montar os cenários.
     * @see {@link UsuarioRepository}
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Repositório de serviços, usado para montar os cenários.
     * @see {@link ServicoRepository}
     */
    @Autowired
    private ServicoRepository servicoRepository;

    /**
     * Primeiro usuário dos cenários.
     * @see {@link UsuarioEntity}
     */
    private UsuarioEntity ana;

    /**
     * Segundo usuário dos cenários.
     * @see {@link UsuarioEntity}
     */
    private UsuarioEntity beto;

    /**
     * Primeiro serviço dos cenários.
     * @see {@link ServicoEntity}
     */
    private ServicoEntity corte;

    /**
     * Segundo serviço dos cenários.
     * @see {@link ServicoEntity}
     */
    private ServicoEntity barba;

    @BeforeEach
    void prepararAgendamentos() {
        ana = salvarUsuario("Ana", "ana@trimly.com");
        beto = salvarUsuario("Beto", "beto@trimly.com");
        corte = salvarServico("Corte");
        barba = salvarServico("Barba");
        salvar(ana, corte, DIA.atTime(9, 0), AgendamentoStatus.AGENDADO);
        salvar(beto, barba, DIA.atTime(23, 30), AgendamentoStatus.CANCELADO);
        salvar(ana, barba, DIA.plusDays(1).atStartOfDay(), AgendamentoStatus.AGENDADO);
    }

    @Test
    @DisplayName("Sem filtros devolve todos os agendamentos")
    void testeDadoFiltroVazioQuandoBuscarEntaoRetornaTodos() {
        List<AgendamentoEntity> resultado = buscar(new AgendamentoFilter(null, null, null, null));

        assertThat(resultado).hasSize(3);
    }

    @Test
    @DisplayName("Filtra pelo status")
    void testeDadoStatusQuandoBuscarEntaoRetornaSoAgendamentosDoStatus() {
        List<AgendamentoEntity> resultado = buscar(new AgendamentoFilter(AgendamentoStatus.CANCELADO, null, null, null));

        assertThat(resultado).extracting(AgendamentoEntity::getUsuario).containsExactly(beto);
    }

    @Test
    @DisplayName("Filtra pelo dia inteiro, sem incluir o início do dia seguinte")
    void testeDadoDataQuandoBuscarEntaoRetornaSoAgendamentosDoDia() {
        List<AgendamentoEntity> resultado = buscar(new AgendamentoFilter(null, DIA, null, null));

        assertThat(resultado).extracting(AgendamentoEntity::getData)
                .containsExactlyInAnyOrder(DIA.atTime(9, 0), DIA.atTime(23, 30));
    }

    @Test
    @DisplayName("Filtra pelo usuário")
    void testeDadoUsuarioQuandoBuscarEntaoRetornaSoAgendamentosDoUsuario() {
        List<AgendamentoEntity> resultado = buscar(new AgendamentoFilter(null, null, ana.getId(), null));

        assertThat(resultado).hasSize(2).allSatisfy(agendamento -> assertThat(agendamento.getUsuario()).isEqualTo(ana));
    }

    @Test
    @DisplayName("Filtra pelo serviço")
    void testeDadoServicoQuandoBuscarEntaoRetornaSoAgendamentosDoServico() {
        List<AgendamentoEntity> resultado = buscar(new AgendamentoFilter(null, null, null, corte.getId()));

        assertThat(resultado).extracting(AgendamentoEntity::getServico).containsExactly(corte);
    }

    @Test
    @DisplayName("Combina todos os filtros informados")
    void testeDadoTodosOsFiltrosQuandoBuscarEntaoRetornaSoOQueAtendeATodos() {
        List<AgendamentoEntity> resultado = buscar(
                new AgendamentoFilter(AgendamentoStatus.AGENDADO, DIA, ana.getId(), corte.getId())
        );

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Filtros sem correspondência devolvem lista vazia")
    void testeDadoFiltrosSemCorrespondenciaQuandoBuscarEntaoRetornaListaVazia() {
        List<AgendamentoEntity> resultado = buscar(
                new AgendamentoFilter(AgendamentoStatus.CANCELADO, DIA, ana.getId(), null)
        );

        assertThat(resultado).isEmpty();
    }

    private List<AgendamentoEntity> buscar(AgendamentoFilter filtro) {
        return repository.findAll(AgendamentoSpecification.comFiltros(filtro));
    }

    private UsuarioEntity salvarUsuario(String nome, String email) {
        return usuarioRepository
                .save(UsuarioEntity.builder().nome(nome).email(email).senha("hash").cargo(UsuarioCargo.CLIENTE).build());
    }

    private ServicoEntity salvarServico(String nome) {
        return servicoRepository.save(
                ServicoEntity.builder()
                        .nome(nome)
                        .valor(new BigDecimal("40.00"))
                        .duracao(30)
                        .status(ServicoStatus.ATIVO)
                        .build()
        );
    }

    private void salvar(UsuarioEntity usuario, ServicoEntity servico, LocalDateTime data, AgendamentoStatus status) {
        repository.save(
                AgendamentoEntity.builder()
                        .data(data)
                        .duracao(30)
                        .status(status)
                        .usuario(usuario)
                        .servico(servico)
                        .build()
        );
    }
}
