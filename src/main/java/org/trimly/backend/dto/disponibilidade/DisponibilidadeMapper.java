package org.trimly.backend.dto.disponibilidade;

import org.springframework.stereotype.Component;
import org.trimly.backend.entity.DisponibilidadeEntity;

@Component
public class DisponibilidadeMapper {
    public DisponibilidadeEntity toEntity(DisponibilidadeCreateDTO request) {
        DisponibilidadeEntity entity = new DisponibilidadeEntity();

        entity.setDiaSemana(request.getDiaSemana());
        entity.setHoraInicio(request.getHoraInicio());
        entity.setHoraFim(request.getHoraFim());

        return entity;
    }

    public DisponibilidadeResponseDTO toResponse(DisponibilidadeEntity entity) {
        DisponibilidadeResponseDTO response = new DisponibilidadeResponseDTO();

        response.setId(entity.getId());
        response.setDiaSemana(entity.getDiaSemana());
        response.setHoraInicio(entity.getHoraInicio());
        response.setHoraFim(entity.getHoraFim());

        return response;
    }
}
