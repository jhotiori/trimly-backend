package org.trimly.backend.model.service.disponibilidade;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;
import org.trimly.backend.model.repository.DisponibilidadeRepository;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

/**
 * Orquestra o CRUD das janelas de atendimento, validado por {@link DisponibilidadeValidator}.
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadeService {
    /**
     * Repositório de disponibilidades.
     * @see {@link DisponibilidadeRepository}
     */
    private final DisponibilidadeRepository repository;

    /**
     * Mapper de disponibilidades.
     * @see {@link DisponibilidadeMapper}
     */
    private final DisponibilidadeMapper mapper;

    /**
     * Validações das regras de disponibilidade.
     * @see {@link DisponibilidadeValidator}
     */
    private final DisponibilidadeValidator disponibilidadeValidator;

    /**
     * Cria uma nova disponibilidade após validar os horários informados.
     *
     * @param request - dados da disponibilidade a ser criada
     * @throws DisponibilidadeHorarioInvalidoException - quando a hora de início não é anterior à de fim
     * @throws DisponibilidadeConflitoException - quando a janela se sobrepõe a outra do mesmo dia
     * @return DisponibilidadeEntity - a disponibilidade criada e persistida
     */
    @Transactional
    public DisponibilidadeEntity create(DisponibilidadeCreateDTO request) {
        DisponibilidadeEntity entity = mapper.toEntity(request);
        disponibilidadeValidator.validateHorarios(entity.getHoraInicio(), entity.getHoraFim());
        disponibilidadeValidator
                .validateConflitoDeHorario(null, entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFim());

        return repository.save(entity);
    }

    /**
     * Atualiza os campos informados de uma disponibilidade existente e revalida os horários.
     *
     * Uma requisição sem nenhum campo informado é um no-op: a disponibilidade é
     * retornada inalterada, sem escrita nem revalidação.
     *
     * @param id - identificador da disponibilidade a ser atualizada
     * @param request - campos a atualizar (dia da semana e/ou horários)
     * @throws EntityNotFoundException - quando não existe disponibilidade com o id informado
     * @throws DisponibilidadeHorarioInvalidoException - quando a hora de início não é anterior à de fim
     * @throws DisponibilidadeConflitoException - quando a janela se sobrepõe a outra do mesmo dia
     * @return DisponibilidadeEntity - a disponibilidade atualizada
     */
    @Transactional
    public DisponibilidadeEntity update(Long id, DisponibilidadeUpdateDTO request) {
        DisponibilidadeEntity entity = this.findById(id);

        if (request.diaSemana() == null && request.horaInicio() == null && request.horaFim() == null) {
            return entity;
        }

        entity.setDiaSemana(Objects.requireNonNullElse(request.diaSemana(), entity.getDiaSemana()));
        entity.setHoraInicio(Objects.requireNonNullElse(request.horaInicio(), entity.getHoraInicio()));
        entity.setHoraFim(Objects.requireNonNullElse(request.horaFim(), entity.getHoraFim()));

        disponibilidadeValidator.validateHorarios(entity.getHoraInicio(), entity.getHoraFim());
        disponibilidadeValidator
                .validateConflitoDeHorario(id, entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFim());
        return repository.save(entity);
    }

    /**
     * Remove a disponibilidade com o identificador informado.
     *
     * @param id - identificador da disponibilidade a ser removida
     * @throws EntityNotFoundException - quando não existe disponibilidade com o id informado
     */
    @Transactional
    public void deleteById(Long id) {
        DisponibilidadeEntity entity = this.findById(id);
        repository.delete(entity);
    }

    /**
     * Retorna todas as disponibilidades cadastradas.
     *
     * @return List - lista de todas as disponibilidades
     */
    public List<DisponibilidadeEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Busca uma disponibilidade pelo seu identificador.
     *
     * @param id - identificador da disponibilidade
     * @throws EntityNotFoundException - quando não existe disponibilidade com o id informado
     * @return DisponibilidadeEntity - a disponibilidade encontrada
     */
    public DisponibilidadeEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disponibilidade não foi encontrada"));
    }

    /**
     * Retorna as disponibilidades cadastradas para o dia da semana informado.
     *
     * @param diaSemana - dia da semana usado no filtro
     * @return List - lista de disponibilidades do dia informado
     */
    public List<DisponibilidadeEntity> findByDiaSemana(DiaSemana diaSemana) {
        return repository.findByDiaSemana(diaSemana);
    }
}
