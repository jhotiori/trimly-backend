package org.trimly.backend.model.service.agendamento;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoForaDoHorarioException;
import org.trimly.backend.model.exception.agendamento.AgendamentoSemDisponibilidadeException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.AgendamentoSpecification;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Serviço de agendamentos. Carrega usuário e serviço, delega a {@link AgendamentoValidator} a
 * validação de horário, disponibilidade e conflito, e orquestra a criação, atualização, consulta e
 * remoção.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamentoService {
    /**
     * Serviço de usuários, usado para carregar o usuário do agendamento.
     * @see {@link UsuarioService}
     */
    private final UsuarioService usuarioService;

    /**
     * Serviço de serviços, usado para carregar o serviço agendado e sua duração.
     * @see {@link ServicoService}
     */
    private final ServicoService servicoService;

    /**
     * Repositório de agendamentos.
     * @see {@link AgendamentoRepository}
     */
    private final AgendamentoRepository repository;

    /**
     * Mapper de agendamentos.
     * @see {@link AgendamentoMapper}
     */
    private final AgendamentoMapper mapper;

    /**
     * Validações das regras de agendamento.
     * @see {@link AgendamentoValidator}
     */
    private final AgendamentoValidator agendamentoValidator;

    /**
     * Cria um novo agendamento após validar horário, disponibilidade e conflitos.
     *
     * @param request - dados do agendamento a ser criado
     * @throws EntityNotFoundException - quando o usuário ou o serviço informado não existe
     * @throws AgendamentoForaDoHorarioException - quando o agendamento ultrapassa o limite de um dia
     * @throws AgendamentoSemDisponibilidadeException - quando não há disponibilidade que comporte o horário
     * @throws AgendamentoConflitoException - quando o horário conflita com outro agendamento
     * @return AgendamentoEntity - o agendamento criado e persistido
     */
    @Transactional
    public AgendamentoEntity create(AgendamentoCreateDTO request) {
        UsuarioEntity usuario = usuarioService.findById(request.usuarioId());
        ServicoEntity servico = servicoService.findById(request.servicoId());
        AgendamentoEntity entity = mapper.toEntity(request, usuario, servico);

        LocalDateTime inicioAgendamento = entity.getData();
        LocalDateTime fimAgendamento = calculateFimAgendamento(inicioAgendamento, servico.getDuracao());

        log.debug("validate agendamento horario futuro: inicio={}, fim={}", inicioAgendamento, fimAgendamento);
        agendamentoValidator.validateHorarioFuturo(inicioAgendamento, fimAgendamento);

        log.debug("validate agendamento disponibilidade: inicio={}, fim={}", inicioAgendamento, fimAgendamento);
        agendamentoValidator.validateDisponibilidade(inicioAgendamento, fimAgendamento);

        log.debug("validate agendamento conflito: inicio={}, fim={}", inicioAgendamento, fimAgendamento);
        agendamentoValidator.validateConflitoDeHorario(null, inicioAgendamento, fimAgendamento);

        entity = repository.save(entity);
        return entity;
    }

    /**
     * Atualiza a data, o serviço e/ou o status de um agendamento existente, revalidando as regras de horário.
     *
     * Uma requisição sem nenhum campo informado é um no-op: o agendamento é retornado
     * inalterado, sem escrita nem revalidação.
     *
     * @param id - identificador do agendamento a ser atualizado
     * @param request - campos a atualizar (data, serviço e/ou status)
     * @throws EntityNotFoundException - quando o agendamento ou o serviço informado não existe
     * @throws AgendamentoException - quando o agendamento não está agendado ou a transição de status não é permitida
     * @throws AgendamentoForaDoHorarioException - quando o agendamento ultrapassa o limite de um dia
     * @throws AgendamentoSemDisponibilidadeException - quando não há disponibilidade que comporte o horário
     * @throws AgendamentoConflitoException - quando o horário conflita com outro agendamento
     * @return AgendamentoEntity - o agendamento atualizado
     */
    @Transactional
    public AgendamentoEntity update(Long id, AgendamentoUpdateDTO request) {
        AgendamentoEntity entity = this.findById(id);

        if (request.data() == null && request.servicoId() == null && request.status() == null) {
            return entity;
        }

        log.debug("validate agendamento status update: id={}, status={}", id, request.status());
        agendamentoValidator.validateStatusUpdate(entity, request.status());

        // 1. Determina o novo horário
        LocalDateTime novaData = request.data() != null ? request.data() : entity.getData();

        // 2. Determina o novo serviço e a nova duração
        ServicoEntity novoServico = entity.getServico();
        Integer novaDuracao = entity.getDuracao();
        if (request.servicoId() != null) {
            novoServico = servicoService.findById(request.servicoId());
            novaDuracao = novoServico.getDuracao();
        }

        // 3. Calcula o novo fim
        LocalDateTime novoFimData = calculateFimAgendamento(novaData, novaDuracao);

        log.debug("validate agendamento horario futuro: inicio={}, fim={}", novaData, novoFimData);
        agendamentoValidator.validateHorarioFuturo(novaData, novoFimData);

        log.debug("validate agendamento disponibilidade: inicio={}, fim={}", novaData, novoFimData);
        agendamentoValidator.validateDisponibilidade(novaData, novoFimData);

        log.debug("validate agendamento conflito: id={}, inicio={}, fim={}", id, novaData, novoFimData);
        agendamentoValidator.validateConflitoDeHorario(entity.getId(), novaData, novoFimData);

        // 4. Aplica as alterações
        entity.setData(novaData);
        entity.setServico(novoServico);
        entity.setDuracao(novaDuracao);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        // 5. Salva
        entity = repository.save(entity);
        return entity;
    }

    /**
     * Retorna todos os agendamentos cadastrados.
     *
     * @return List - lista de todos os agendamentos
     */
    public List<AgendamentoEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Retorna os agendamentos que atendem aos filtros informados.
     *
     * @param filtro - critérios de status, data, usuário e serviço
     * @return List - lista de agendamentos filtrados
     */
    public List<AgendamentoEntity> findAll(AgendamentoFilter filtro) {
        return repository.findAll(AgendamentoSpecification.comFiltros(filtro));
    }

    /**
     * Busca um agendamento pelo seu identificador.
     *
     * @param id - identificador do agendamento
     * @throws EntityNotFoundException - quando não existe agendamento com o id informado
     * @return AgendamentoEntity - o agendamento encontrado
     */
    public AgendamentoEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agendamento não foi encontrado"));
    }

    /**
     * Retorna os agendamentos com o status informado.
     *
     * @param status - status usado no filtro
     * @return List - lista de agendamentos com o status informado
     */
    public List<AgendamentoEntity> findByStatus(AgendamentoStatus status) {
        return repository.findByStatus(status);
    }

    /**
     * Retorna os agendamentos com o status informado dentro do período indicado.
     *
     * @param status - status usado no filtro
     * @param inicio - início do período, inclusivo
     * @param fim - fim do período, exclusivo
     * @return List - lista de agendamentos no período
     */
    public List<AgendamentoEntity> findByStatusAndPeriodo(
            AgendamentoStatus status, LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(status, inicio, fim);
    }

    /**
     * Remove o agendamento com o identificador informado.
     *
     * @param id - identificador do agendamento a ser removido
     * @throws EntityNotFoundException - quando não existe agendamento com o id informado
     */
    @Transactional
    public void deleteById(Long id) {
        AgendamentoEntity entity = this.findById(id);
        repository.delete(entity);
    }

    /**
     * Calcula o horário de fim de um agendamento.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @param duracao - duração do serviço em minutos
     * @return LocalDateTime - data e hora de fim calculada
     */
    private LocalDateTime calculateFimAgendamento(LocalDateTime inicioAgendamento, Integer duracao) {
        return inicioAgendamento.plusMinutes(duracao);
    }
}
