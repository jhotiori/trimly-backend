package org.trimly.backend.model.service.agendamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.agendamento.AgendamentoAntecedenciaExcedidaException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoForaDoHorarioException;
import org.trimly.backend.model.exception.agendamento.AgendamentoSemDisponibilidadeException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.AgendamentoSpecification;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Orquestra o CRUD de agendamentos, validado por {@link AgendamentoValidator}.
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
     * @throws AgendamentoAntecedenciaExcedidaException - quando o início ultrapassa o limite de 14 dias de antecedência
     * @throws AgendamentoSemDisponibilidadeException - quando não há disponibilidade que comporte o horário
     * @throws AgendamentoConflitoException - quando o horário conflita com outro agendamento
     * @return AgendamentoEntity - o agendamento criado e persistido
     */
    @Transactional
    public AgendamentoEntity create(AgendamentoCreateDTO request) {
        UsuarioEntity usuario = usuarioService.findById(request.usuarioId());
        ServicoEntity servico = servicoService.findById(request.servicoId());
        AgendamentoEntity entity = mapper.toEntity(request, usuario, servico);

        validateHorario(null, entity.getData(), servico.getDuracao());

        return repository.save(entity);
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
     * @throws AgendamentoStatusException - quando o agendamento não está agendado ou a transição de status não é permitida
     * @throws AgendamentoForaDoHorarioException - quando o agendamento ultrapassa o limite de um dia
     * @throws AgendamentoAntecedenciaExcedidaException - quando o início ultrapassa o limite de 14 dias de antecedência
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

        LocalDateTime novaData = Objects.requireNonNullElse(request.data(), entity.getData());

        ServicoEntity novoServico = entity.getServico();
        Integer novaDuracao = entity.getDuracao();
        if (request.servicoId() != null) {
            novoServico = servicoService.findById(request.servicoId());
            novaDuracao = novoServico.getDuracao();
        }

        validateHorario(id, novaData, novaDuracao);

        entity.setData(novaData);
        entity.setServico(novoServico);
        entity.setDuracao(novaDuracao);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        return repository.save(entity);
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
     * Executa, na ordem documentada, as validações de data e horário do agendamento.
     *
     * @param id - identificador do agendamento a ignorar no conflito, ou nulo na criação
     * @param inicio - data e hora de início do agendamento
     * @param duracao - duração do serviço em minutos
     */
    private void validateHorario(Long id, LocalDateTime inicio, Integer duracao) {
        LocalDateTime fim = inicio.plusMinutes(duracao);

        log.debug("validate agendamento horario: id={}, inicio={}, fim={}", id, inicio, fim);
        agendamentoValidator.validateIsFeriado(inicio.toLocalDate());
        agendamentoValidator.validateHorarioFuturo(inicio, fim);
        agendamentoValidator.validateLimiteAntecedencia(inicio);
        agendamentoValidator.validateDisponibilidade(inicio, fim);
        agendamentoValidator.validateConflitoDeHorario(id, inicio, fim);
    }
}
