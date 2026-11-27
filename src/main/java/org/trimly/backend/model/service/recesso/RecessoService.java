package org.trimly.backend.model.service.recesso;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNaoEncontradoException;
import org.trimly.backend.model.exception.recesso.RecessoNoPassadoException;
import org.trimly.backend.model.exception.recesso.RecessoPeriodoInvalidoException;
import org.trimly.backend.model.repository.RecessoRepository;
import org.trimly.backend.view.dto.recesso.RecessoCreateDTO;
import org.trimly.backend.view.dto.recesso.RecessoUpdateDTO;
import org.trimly.backend.view.mapper.RecessoMapper;

/**
 * Orquestra o CRUD dos recessos, validado por {@link RecessoValidator}.
 */
@Service
@RequiredArgsConstructor
public class RecessoService {
    /**
     * Repositório de recessos.
     * @see {@link RecessoRepository}
     */
    private final RecessoRepository repository;

    /**
     * Mapper de recessos.
     * @see {@link RecessoMapper}
     */
    private final RecessoMapper mapper;

    /**
     * Validações das regras de recesso.
     * @see {@link RecessoValidator}
     */
    private final RecessoValidator recessoValidator;

    /**
     * Cria um novo recesso após validar o período informado.
     *
     * @param request - dados do recesso a ser criado
     * @throws RecessoNoPassadoException - quando a data de início é anterior a hoje
     * @throws RecessoPeriodoInvalidoException - quando a data de fim é anterior à de início
     * @throws RecessoConflitoException - quando o período se sobrepõe a outro recesso
     * @return RecessoEntity - o recesso criado e persistido
     */
    @Transactional
    public RecessoEntity create(RecessoCreateDTO request) {
        RecessoEntity entity = mapper.toEntity(request);
        recessoValidator.validateNoPassado(entity.getDataInicio());
        recessoValidator.validatePeriodo(entity.getDataInicio(), entity.getDataFim());
        recessoValidator.validateConflito(null, entity.getDataInicio(), entity.getDataFim());

        return repository.save(entity);
    }

    /**
     * Atualiza os campos informados de um recesso existente e revalida o período.
     *
     * Uma requisição sem nenhum campo informado é um no-op: o recesso é retornado inalterado, sem escrita nem
     * revalidação. Só as datas enviadas são checadas contra o passado, então um recesso em andamento aceita
     * alterar a descrição ou o fim.
     *
     * @param id - identificador do recesso a ser atualizado
     * @param request - campos a atualizar (descrição e/ou datas)
     * @throws RecessoNaoEncontradoException - quando não existe recesso com o id informado
     * @throws RecessoNoPassadoException - quando uma data enviada é anterior a hoje
     * @throws RecessoPeriodoInvalidoException - quando a data de fim é anterior à de início
     * @throws RecessoConflitoException - quando o período se sobrepõe a outro recesso
     * @return RecessoEntity - o recesso atualizado
     */
    @Transactional
    public RecessoEntity update(Long id, RecessoUpdateDTO request) {
        RecessoEntity entity = this.findById(id);

        if (request.descricao() == null && request.dataInicio() == null && request.dataFim() == null) {
            return entity;
        }

        if (request.dataInicio() != null) {
            recessoValidator.validateNoPassado(request.dataInicio());
        }

        if (request.dataFim() != null) {
            recessoValidator.validateNoPassado(request.dataFim());
        }

        if (StringUtils.hasText(request.descricao())) {
            entity.setDescricao(request.descricao());
        }

        entity.setDataInicio(Objects.requireNonNullElse(request.dataInicio(), entity.getDataInicio()));
        entity.setDataFim(Objects.requireNonNullElse(request.dataFim(), entity.getDataFim()));

        recessoValidator.validatePeriodo(entity.getDataInicio(), entity.getDataFim());
        recessoValidator.validateConflito(id, entity.getDataInicio(), entity.getDataFim());
        return repository.save(entity);
    }

    /**
     * Remove o recesso com o identificador informado.
     *
     * @param id - identificador do recesso a ser removido
     * @throws RecessoNaoEncontradoException - quando não existe recesso com o id informado
     */
    @Transactional
    public void deleteById(Long id) {
        RecessoEntity entity = this.findById(id);
        repository.delete(entity);
    }

    /**
     * Retorna todos os recessos cadastrados.
     *
     * @return List - lista de todos os recessos
     */
    public List<RecessoEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Busca um recesso pelo seu identificador.
     *
     * @param id - identificador do recesso
     * @throws RecessoNaoEncontradoException - quando não existe recesso com o id informado
     * @return RecessoEntity - o recesso encontrado
     */
    public RecessoEntity findById(Long id) {
        return repository.findById(id).orElseThrow(RecessoNaoEncontradoException::new);
    }
}
