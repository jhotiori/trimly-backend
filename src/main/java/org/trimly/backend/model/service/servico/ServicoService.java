package org.trimly.backend.model.service.servico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.repository.ServicoRepository;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

/**
 * Orquestra o CRUD do catálogo de serviços, validado por {@link ServicoValidator}.
 */
@Service
@RequiredArgsConstructor
public class ServicoService {
    /**
     * Repositório de serviços.
     * @see {@link ServicoRepository}
     */
    private final ServicoRepository repository;

    /**
     * Mapper de serviços.
     * @see {@link ServicoMapper}
     */
    private final ServicoMapper mapper;

    /**
     * Validações das regras de serviço.
     * @see {@link ServicoValidator}
     */
    private final ServicoValidator servicoValidator;

    /**
     * Cria um novo serviço com status ativo após validar a unicidade do nome.
     *
     * @param request - dados do serviço a ser criado
     * @throws ServicoNomeDuplicadoException - quando já existe um serviço com o mesmo nome
     * @return ServicoEntity - o serviço criado e persistido
     */
    @Transactional
    public ServicoEntity create(ServicoCreateDTO request) {
        servicoValidator.validateNomeUnico(request.nome());

        return repository.save(mapper.toEntity(request));
    }

    /**
     * Atualiza os campos informados de um serviço existente.
     *
     * Uma requisição sem nenhum campo informado é um no-op: o serviço é retornado
     * inalterado, sem escrita nem revalidação.
     *
     * @param id - identificador do serviço a ser atualizado
     * @param request - campos a atualizar (nome, valor, duração e/ou status)
     * @throws EntityNotFoundException - quando não existe serviço com o id informado
     * @throws ServicoException - quando a transição de status não é permitida
     * @return ServicoEntity - o serviço atualizado
     */
    @Transactional
    public ServicoEntity update(Long id, ServicoUpdateDTO request) {
        ServicoEntity entity = this.findById(id);

        if (request.nome() == null && request.valor() == null && request.duracao() == null
                && request.status() == null) {
            return entity;
        }

        servicoValidator.validateStatusUpdate(entity, request.status());

        if (StringUtils.hasText(request.nome())) {
            entity.setNome(request.nome());
        }

        BigDecimal valor = request.valor();
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            entity.setValor(valor);
        }

        Integer duracao = request.duracao();
        if (duracao != null && duracao > 0) {
            entity.setDuracao(duracao);
        }

        entity.setStatus(Objects.requireNonNullElse(request.status(), entity.getStatus()));

        return repository.save(entity);
    }

    /**
     * Remove o serviço com o identificador informado.
     *
     * @param id - identificador do serviço a ser removido
     * @throws EntityNotFoundException - quando não existe serviço com o id informado
     * @throws ServicoComAgendamentoPendenteException - quando o serviço possui agendamento em `AGENDADO`
     */
    @Transactional
    public void deleteById(Long id) {
        ServicoEntity entity = this.findById(id);
        servicoValidator.validateSemAgendamentoPendente(id);
        repository.delete(entity);
    }

    /**
     * Retorna todos os serviços cadastrados.
     *
     * @return List - lista de todos os serviços
     */
    public List<ServicoEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Busca um serviço pelo seu identificador.
     *
     * @param id - identificador do serviço
     * @throws EntityNotFoundException - quando não existe serviço com o id informado
     * @return ServicoEntity - o serviço encontrado
     */
    public ServicoEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));
    }

    /**
     * Retorna os serviços com o status informado.
     *
     * @param status - status usado no filtro
     * @return List - lista de serviços com o status informado
     */
    public List<ServicoEntity> findByStatus(ServicoStatus status) {
        return repository.findByStatus(status);
    }
}
