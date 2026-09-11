package org.trimly.backend.view.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeResponseDTO;

/**
 * Mapper de disponibilidades. Converte à mão entre {@code DisponibilidadeEntity} e seus DTOs, sem
 * MapStruct.
 */
@Component
public class DisponibilidadeMapper {
    /**
     * Converte os dados de criação em uma entidade de disponibilidade.
     *
     * @param request - dados de criação da disponibilidade
     * @return DisponibilidadeEntity - a entidade preenchida a partir dos dados informados
     */
    public DisponibilidadeEntity toEntity(DisponibilidadeCreateDTO request) {
        DisponibilidadeEntity entity = new DisponibilidadeEntity();

        entity.setDiaSemana(request.diaSemana());
        entity.setHoraInicio(request.horaInicio());
        entity.setHoraFim(request.horaFim());

        return entity;
    }

    /**
     * Converte uma lista de dados de criação em uma lista de entidades de
     * disponibilidade.
     *
     * @param original - lista de dados de criação a serem convertidos
     * @return List - lista de entidades correspondentes
     */
    public List<DisponibilidadeEntity> toEntityList(List<? extends DisponibilidadeCreateDTO> original) {
        return original.stream().map(this::toEntity).toList();
    }

    /**
     * Converte uma entidade de disponibilidade em seu DTO de resposta.
     *
     * @param entity - entidade a ser convertida
     * @return DisponibilidadeResponseDTO - o DTO de resposta correspondente
     */
    public DisponibilidadeResponseDTO toResponse(DisponibilidadeEntity entity) {
        return new DisponibilidadeResponseDTO(
                entity.getId(), entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFim());
    }

    /**
     * Converte uma lista de entidades de disponibilidade em uma lista de DTOs de resposta.
     *
     * @param original - lista de entidades a serem convertidas
     * @return List - lista de DTOs de resposta correspondentes
     */
    public List<DisponibilidadeResponseDTO> toResponseList(List<? extends DisponibilidadeEntity> original) {
        return original.stream().map(this::toResponse).toList();
    }
}
