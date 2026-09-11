package org.trimly.backend.model.service.servico;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * Serviço de serviços. Valida a unicidade do nome e as transições de status via
 * {@link ServicoValidator} e orquestra a criação, atualização, consulta e remoção do catálogo.
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
        ServicoEntity entity = mapper.toEntity(request);
        entity.setStatus(ServicoStatus.ATIVO);

        servicoValidator.validateNomeUnico(request.nome());

        entity = repository.save(entity);
        return entity;
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

        if (request.nome() == null
                && request.valor() == null
                && request.duracao() == null
                && request.status() == null) {
            return entity;
        }

        servicoValidator.validateStatusUpdate(entity, request.status());

        String nome = request.nome();
        if (nome != null && !nome.isBlank()) {
            entity.setNome(nome);
        }

        BigDecimal valor = request.valor();
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            entity.setValor(valor);
        }

        Integer duracao = request.duracao();
        if (duracao != null && duracao > 0) {
            entity.setDuracao(duracao);
        }

        ServicoStatus status = request.status();
        if (status != null) {
            entity.setStatus(status);
        }

        entity = repository.save(entity);
        return entity;
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
     * Retorna os serviços cujo nome corresponde ao termo informado.
     *
     * @param nome - termo de busca aplicado ao nome
     * @return List - lista de serviços correspondentes
     */
    public List<ServicoEntity> findByNome(String nome) {
        return repository.findByNomeLikeIgnoreCase(nome);
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

    /**
     * Remove o serviço com o identificador informado.
     *
     * @param id - identificador do serviço a ser removido
     * @throws EntityNotFoundException - quando não existe serviço com o id informado
     * @throws ServicoComAgendamentoPendenteException - quando o serviço possui agendamento em {@code AGENDADO}
     */
    @Transactional
    public void deleteById(Long id) {
        ServicoEntity entity = this.findById(id);
        servicoValidator.validateSemAgendamentoPendente(id);
        repository.delete(entity);
    }
}
