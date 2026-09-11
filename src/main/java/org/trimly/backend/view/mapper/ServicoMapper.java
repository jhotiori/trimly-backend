package org.trimly.backend.view.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoResponseDTO;

/**
 * Mapper de serviços. Converte à mão entre {@code ServicoEntity} e seus DTOs, sem MapStruct.
 */
@Component
public class ServicoMapper {
    /**
     * Converte os dados de criação em uma entidade de serviço com status ativo.
     *
     * @param request - dados de criação do serviço
     * @return ServicoEntity - a entidade preenchida a partir dos dados informados
     */
    public ServicoEntity toEntity(ServicoCreateDTO request) {
        ServicoEntity entity = new ServicoEntity();

        entity.setNome(request.nome());
        entity.setValor(request.valor());
        entity.setDuracao(request.duracao());
        entity.setStatus(ServicoStatus.ATIVO);

        return entity;
    }

    /**
     * Converte uma lista de dados de criação em uma lista de entidades de serviço.
     *
     * @param original - lista de dados de criação a serem convertidos
     * @return List - lista de entidades correspondentes
     */
    public List<ServicoEntity> toEntityList(List<? extends ServicoCreateDTO> original) {
        return original.stream().map(this::toEntity).toList();
    }

    /**
     * Converte uma entidade de serviço em seu DTO de resposta.
     *
     * @param entity - entidade a ser convertida
     * @return ServicoResponseDTO - o DTO de resposta correspondente
     */
    public ServicoResponseDTO toResponse(ServicoEntity entity) {
        return new ServicoResponseDTO(
                entity.getId(), entity.getNome(), entity.getValor(), entity.getDuracao(), entity.getStatus());
    }

    /**
     * Converte uma lista de entidades de serviço em uma lista de DTOs de resposta.
     *
     * @param original - lista de entidades a serem convertidas
     * @return List - lista de DTOs de resposta correspondentes
     */
    public List<ServicoResponseDTO> toResponseList(List<? extends ServicoEntity> original) {
        return original.stream().map(this::toResponse).toList();
    }
}
