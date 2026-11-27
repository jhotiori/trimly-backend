package org.trimly.backend.view.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.view.dto.recesso.RecessoCreateDTO;
import org.trimly.backend.view.dto.recesso.RecessoResponseDTO;

/**
 * Converte entre `RecessoEntity` e seus DTOs, sem MapStruct.
 */
@Component
public class RecessoMapper {
    /**
     * Converte os dados de criação em uma entidade de recesso.
     *
     * @param request - dados de criação do recesso
     * @return RecessoEntity - a entidade preenchida a partir dos dados informados
     */
    public RecessoEntity toEntity(RecessoCreateDTO request) {
        RecessoEntity entity = new RecessoEntity();

        entity.setDescricao(request.descricao());
        entity.setDataInicio(request.dataInicio());
        entity.setDataFim(request.dataFim());

        return entity;
    }

    /**
     * Converte uma lista de dados de criação em uma lista de entidades de recesso.
     *
     * @param original - lista de dados de criação a serem convertidos
     * @return List - lista de entidades correspondentes
     */
    public List<RecessoEntity> toEntityList(List<RecessoCreateDTO> original) {
        return original.stream().map(this::toEntity).toList();
    }

    /**
     * Converte uma entidade de recesso em seu DTO de resposta.
     *
     * @param entity - entidade a ser convertida
     * @return RecessoResponseDTO - o DTO de resposta correspondente
     */
    public RecessoResponseDTO toResponse(RecessoEntity entity) {
        return new RecessoResponseDTO(
                entity.getId(),
                entity.getDescricao(),
                entity.getDataInicio(),
                entity.getDataFim()
        );
    }

    /**
     * Converte uma lista de entidades de recesso em uma lista de DTOs de resposta.
     *
     * @param original - lista de entidades a serem convertidas
     * @return List - lista de DTOs de resposta correspondentes
     */
    public List<RecessoResponseDTO> toResponseList(List<? extends RecessoEntity> original) {
        return original.stream().map(this::toResponse).toList();
    }
}
