package org.trimly.backend.model.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

/**
 * Testes para {@link AgendamentoRepository}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AgendamentoRepositoryTests {
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

    @Test
    @DisplayName("Lista agendados do dia, incluindo 00:00 e excluindo o início do dia seguinte")
    void testeDadoAgendamentosEmDiasEStatusDiferentesQuandoBuscarDoDiaEntaoRetornaSoAgendadosDoIntervalo() {
        UsuarioEntity usuario = salvarUsuario();
        ServicoEntity servico = salvarServico();
        salvar(usuario, servico, DIA.atStartOfDay(), AgendamentoStatus.AGENDADO);
        salvar(usuario, servico, DIA.atTime(15, 0), AgendamentoStatus.AGENDADO);
        salvar(usuario, servico, DIA.atTime(10, 0), AgendamentoStatus.CANCELADO);
        salvar(usuario, servico, DIA.plusDays(1).atStartOfDay(), AgendamentoStatus.AGENDADO);

        List<AgendamentoEntity> resultado = repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                AgendamentoStatus.AGENDADO,
                DIA.atStartOfDay(),
                DIA.plusDays(1).atStartOfDay()
        );

        assertThat(resultado).extracting(AgendamentoEntity::getData)
                .containsExactlyInAnyOrder(DIA.atStartOfDay(), DIA.atTime(15, 0));
    }

    @Test
    @DisplayName("Detecta agendamento futuro do serviço após a referência")
    void testeDadoAgendamentoAposAReferenciaQuandoVerificarServicoComFuturoEntaoRetornaVerdadeiro() {
        ServicoEntity servico = salvarServico();
        salvar(salvarUsuario(), servico, DIA.atTime(9, 0), AgendamentoStatus.AGENDADO);

        assertTrue(
                repository.existsByServicoIdAndStatusAndDataAfter(
                        servico.getId(),
                        AgendamentoStatus.AGENDADO,
                        DIA.atStartOfDay()
                )
        );
    }

    @Test
    @DisplayName("Ignora agendamento do serviço anterior à referência ou com outro status")
    void testeDadoAgendamentoAntesDaReferenciaOuCanceladoQuandoVerificarServicoComFuturoEntaoRetornaFalso() {
        UsuarioEntity usuario = salvarUsuario();
        ServicoEntity servico = salvarServico();
        salvar(usuario, servico, DIA.atTime(9, 0), AgendamentoStatus.AGENDADO);
        salvar(usuario, servico, DIA.plusDays(2).atTime(9, 0), AgendamentoStatus.CANCELADO);

        assertFalse(
                repository.existsByServicoIdAndStatusAndDataAfter(
                        servico.getId(),
                        AgendamentoStatus.AGENDADO,
                        DIA.plusDays(1).atStartOfDay()
                )
        );
    }

    @Test
    @DisplayName("Detecta agendamento do usuário apenas no status informado")
    void testeDadoAgendamentoDoUsuarioQuandoVerificarPorUsuarioEStatusEntaoConsideraSoOStatus() {
        UsuarioEntity usuario = salvarUsuario();
        salvar(usuario, salvarServico(), DIA.atTime(9, 0), AgendamentoStatus.AGENDADO);

        assertTrue(repository.existsByUsuarioIdAndStatus(usuario.getId(), AgendamentoStatus.AGENDADO));
        assertFalse(repository.existsByUsuarioIdAndStatus(usuario.getId(), AgendamentoStatus.CONCLUIDO));
    }

    @Test
    @DisplayName("Detecta agendamento do serviço apenas no status informado")
    void testeDadoAgendamentoDoServicoQuandoVerificarPorServicoEStatusEntaoConsideraSoOStatus() {
        ServicoEntity servico = salvarServico();
        salvar(salvarUsuario(), servico, DIA.atTime(9, 0), AgendamentoStatus.AGENDADO);

        assertTrue(repository.existsByServicoIdAndStatus(servico.getId(), AgendamentoStatus.AGENDADO));
        assertFalse(repository.existsByServicoIdAndStatus(servico.getId(), AgendamentoStatus.AUSENTE));
    }

    private UsuarioEntity salvarUsuario() {
        return usuarioRepository.save(
                UsuarioEntity.builder()
                        .nome("Ana")
                        .email("ana@trimly.com")
                        .senha("hash")
                        .cargo(UsuarioCargo.CLIENTE)
                        .build()
        );
    }

    private ServicoEntity salvarServico() {
        return servicoRepository.save(
                ServicoEntity.builder()
                        .nome("Corte")
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
