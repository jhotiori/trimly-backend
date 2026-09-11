package org.trimly.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.trimly.backend.model.entity.AgendamentoEntity;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.entity.enums.ServicoStatus;
import org.trimly.backend.model.entity.enums.UsuarioCargo;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.AgendamentoSpecification;
import org.trimly.backend.model.repository.ServicoRepository;
import org.trimly.backend.model.repository.UsuarioRepository;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;

@RepositoryTest
class AgendamentoRepositoryTests {
    @Autowired
    private AgendamentoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    /**
     * Verifica os limites de {@code findByStatusAndDataGreaterThanEqualAndDataLessThan}: um agendamento
     * exatamente no início do período é incluído, e um exatamente no fim é excluído.
     */
    @Test
    void agendamento_TestFindByStatusAndDataGreaterThanEqualAndDataLessThanRespeitaLimites() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));

        LocalDate dataDoDia = LocalDate.of(2026, 9, 10);
        LocalDateTime inicioDoDia = dataDoDia.atStartOfDay();
        LocalDateTime inicioDoProximoDia = dataDoDia.plusDays(1).atStartOfDay();

        AgendamentoEntity noInicio =
                repository.saveAndFlush(criarAgendamento(usuario, servico, inicioDoDia, AgendamentoStatus.AGENDADO));
        AgendamentoEntity noFim = repository.saveAndFlush(
                criarAgendamento(usuario, servico, inicioDoProximoDia, AgendamentoStatus.AGENDADO));

        List<AgendamentoEntity> resultado = repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                AgendamentoStatus.AGENDADO, inicioDoDia, inicioDoProximoDia);

        assertThat(resultado)
                .extracting(AgendamentoEntity::getId)
                .contains(noInicio.getId())
                .doesNotContain(noFim.getId());
    }

    /**
     * Verifica que {@code existsByServicoIdAndStatusAndDataAfter} retorna {@code true} quando existe um
     * agendamento do serviço, no status informado, marcado após a data de referência.
     */
    @Test
    void agendamento_TestExistsByServicoIdAndStatusAndDataAfterComAgendamentoFuturoRetornaTrue() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));
        LocalDateTime referencia = LocalDateTime.of(2026, 9, 10, 0, 0);

        repository.saveAndFlush(criarAgendamento(usuario, servico, referencia.plusDays(1), AgendamentoStatus.AGENDADO));

        assertThat(repository.existsByServicoIdAndStatusAndDataAfter(
                        servico.getId(), AgendamentoStatus.AGENDADO, referencia))
                .isTrue();
    }

    /**
     * Verifica que {@code existsByServicoIdAndStatusAndDataAfter} retorna {@code false} quando não há nenhum
     * agendamento do serviço marcado após a data de referência.
     */
    @Test
    void agendamento_TestExistsByServicoIdAndStatusAndDataAfterSemAgendamentoFuturoRetornaFalse() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));
        LocalDateTime dataDoAgendamento = LocalDateTime.of(2026, 9, 10, 0, 0);

        repository.saveAndFlush(criarAgendamento(usuario, servico, dataDoAgendamento, AgendamentoStatus.AGENDADO));

        assertThat(repository.existsByServicoIdAndStatusAndDataAfter(
                        servico.getId(), AgendamentoStatus.AGENDADO, dataDoAgendamento.plusDays(1)))
                .isFalse();
    }

    /**
     * Verifica que {@code existsByServicoIdAndStatus} retorna {@code true} quando o serviço tem um agendamento
     * no status informado.
     */
    @Test
    void agendamento_TestExistsByServicoIdAndStatusComAgendamentoNoStatusRetornaTrue() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));

        repository.saveAndFlush(
                criarAgendamento(usuario, servico, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.AGENDADO));

        assertThat(repository.existsByServicoIdAndStatus(servico.getId(), AgendamentoStatus.AGENDADO))
                .isTrue();
    }

    /**
     * Verifica que {@code existsByServicoIdAndStatus} retorna {@code false} quando o serviço não tem nenhum
     * agendamento no status informado.
     */
    @Test
    void agendamento_TestExistsByServicoIdAndStatusSemAgendamentoNoStatusRetornaFalse() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));

        repository.saveAndFlush(
                criarAgendamento(usuario, servico, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.CANCELADO));

        assertThat(repository.existsByServicoIdAndStatus(servico.getId(), AgendamentoStatus.AGENDADO))
                .isFalse();
    }

    /**
     * Verifica que {@code existsByUsuarioIdAndStatus} retorna {@code true} quando o usuário tem um agendamento
     * no status informado.
     */
    @Test
    void agendamento_TestExistsByUsuarioIdAndStatusComAgendamentoNoStatusRetornaTrue() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));

        repository.saveAndFlush(
                criarAgendamento(usuario, servico, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.AGENDADO));

        assertThat(repository.existsByUsuarioIdAndStatus(usuario.getId(), AgendamentoStatus.AGENDADO))
                .isTrue();
    }

    /**
     * Verifica que {@code existsByUsuarioIdAndStatus} retorna {@code false} quando o usuário não tem nenhum
     * agendamento no status informado.
     */
    @Test
    void agendamento_TestExistsByUsuarioIdAndStatusSemAgendamentoNoStatusRetornaFalse() {
        UsuarioEntity usuario = usuarioRepository.saveAndFlush(criarUsuario("cliente@trimly.com"));
        ServicoEntity servico = servicoRepository.saveAndFlush(criarServico("Corte"));

        repository.saveAndFlush(
                criarAgendamento(usuario, servico, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.CANCELADO));

        assertThat(repository.existsByUsuarioIdAndStatus(usuario.getId(), AgendamentoStatus.AGENDADO))
                .isFalse();
    }

    /**
     * Verifica que {@code AgendamentoSpecification.comFiltros} sem nenhum filtro preenchido retorna todos os
     * agendamentos, confirmando que um filtro totalmente vazio não restringe a consulta.
     */
    @Test
    void agendamento_TestComFiltrosSemNenhumFiltroRetornaTodos() {
        UsuarioEntity usuario1 = usuarioRepository.saveAndFlush(criarUsuario("u1@trimly.com"));
        UsuarioEntity usuario2 = usuarioRepository.saveAndFlush(criarUsuario("u2@trimly.com"));
        ServicoEntity servico1 = servicoRepository.saveAndFlush(criarServico("Corte"));
        ServicoEntity servico2 = servicoRepository.saveAndFlush(criarServico("Barba"));

        repository.saveAndFlush(
                criarAgendamento(usuario1, servico1, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.AGENDADO));
        repository.saveAndFlush(criarAgendamento(
                usuario2, servico1, LocalDateTime.of(2026, 9, 10, 11, 0), AgendamentoStatus.CANCELADO));
        repository.saveAndFlush(
                criarAgendamento(usuario1, servico2, LocalDateTime.of(2026, 9, 11, 10, 0), AgendamentoStatus.AGENDADO));

        List<AgendamentoEntity> resultado =
                repository.findAll(AgendamentoSpecification.comFiltros(new AgendamentoFilter(null, null, null, null)));

        assertThat(resultado).hasSize(3);
    }

    /**
     * Verifica que {@code AgendamentoSpecification.comFiltros} com apenas o status preenchido retorna somente
     * os agendamentos com esse status, sem se importar com usuário, serviço ou data.
     */
    @Test
    void agendamento_TestComFiltrosComApenasStatusRetornaSomenteOStatus() {
        UsuarioEntity usuario1 = usuarioRepository.saveAndFlush(criarUsuario("u1@trimly.com"));
        UsuarioEntity usuario2 = usuarioRepository.saveAndFlush(criarUsuario("u2@trimly.com"));
        ServicoEntity servico1 = servicoRepository.saveAndFlush(criarServico("Corte"));
        ServicoEntity servico2 = servicoRepository.saveAndFlush(criarServico("Barba"));

        AgendamentoEntity agendado1 = repository.saveAndFlush(
                criarAgendamento(usuario1, servico1, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.AGENDADO));
        repository.saveAndFlush(criarAgendamento(
                usuario2, servico1, LocalDateTime.of(2026, 9, 10, 11, 0), AgendamentoStatus.CANCELADO));
        AgendamentoEntity agendado2 = repository.saveAndFlush(
                criarAgendamento(usuario1, servico2, LocalDateTime.of(2026, 9, 11, 10, 0), AgendamentoStatus.AGENDADO));

        AgendamentoFilter filtro = new AgendamentoFilter(AgendamentoStatus.AGENDADO, null, null, null);
        List<AgendamentoEntity> resultado = repository.findAll(AgendamentoSpecification.comFiltros(filtro));

        assertThat(resultado)
                .extracting(AgendamentoEntity::getId)
                .containsExactlyInAnyOrder(agendado1.getId(), agendado2.getId());
    }

    /**
     * Verifica que {@code AgendamentoSpecification.comFiltros} com status e serviço preenchidos combina os dois
     * critérios, retornando apenas o agendamento que atende a ambos.
     */
    @Test
    void agendamento_TestComFiltrosComStatusEServicoCombinaOsCriterios() {
        UsuarioEntity usuario1 = usuarioRepository.saveAndFlush(criarUsuario("u1@trimly.com"));
        ServicoEntity servico1 = servicoRepository.saveAndFlush(criarServico("Corte"));
        ServicoEntity servico2 = servicoRepository.saveAndFlush(criarServico("Barba"));

        AgendamentoEntity esperado = repository.saveAndFlush(
                criarAgendamento(usuario1, servico1, LocalDateTime.of(2026, 9, 10, 10, 0), AgendamentoStatus.AGENDADO));
        repository.saveAndFlush(
                criarAgendamento(usuario1, servico2, LocalDateTime.of(2026, 9, 10, 11, 0), AgendamentoStatus.AGENDADO));
        repository.saveAndFlush(criarAgendamento(
                usuario1, servico1, LocalDateTime.of(2026, 9, 11, 10, 0), AgendamentoStatus.CANCELADO));

        AgendamentoFilter filtro = new AgendamentoFilter(AgendamentoStatus.AGENDADO, null, null, servico1.getId());
        List<AgendamentoEntity> resultado = repository.findAll(AgendamentoSpecification.comFiltros(filtro));

        assertThat(resultado).extracting(AgendamentoEntity::getId).containsExactly(esperado.getId());
    }

    private UsuarioEntity criarUsuario(String email) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome("Nome");
        entity.setEmail(email);
        entity.setSenha("hash");
        entity.setCargo(UsuarioCargo.CLIENTE);
        return entity;
    }

    private ServicoEntity criarServico(String nome) {
        ServicoEntity entity = new ServicoEntity();
        entity.setNome(nome);
        entity.setValor(BigDecimal.TEN);
        entity.setDuracao(30);
        entity.setStatus(ServicoStatus.ATIVO);
        return entity;
    }

    private AgendamentoEntity criarAgendamento(
            UsuarioEntity usuario, ServicoEntity servico, LocalDateTime data, AgendamentoStatus status) {
        AgendamentoEntity entity = new AgendamentoEntity();
        entity.setUsuario(usuario);
        entity.setServico(servico);
        entity.setData(data);
        entity.setDuracao(30);
        entity.setStatus(status);
        return entity;
    }
}
