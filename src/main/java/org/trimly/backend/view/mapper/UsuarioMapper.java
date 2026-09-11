package org.trimly.backend.view.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;

/**
 * Mapper de usuários. Converte à mão entre {@code UsuarioEntity} e seus DTOs, sem MapStruct.
 */
@Component
public class UsuarioMapper {
    /**
     * Converte os dados de criação em uma entidade de usuário.
     *
     * @param request - dados de criação do usuário
     * @return UsuarioEntity - a entidade preenchida a partir dos dados informados
     */
    public UsuarioEntity toEntity(UsuarioCreateDTO request) {
        UsuarioEntity entity = new UsuarioEntity();

        entity.setNome(request.nome());
        entity.setEmail(request.email());

        return entity;
    }

    /**
     * Converte uma lista de dados de criação em uma lista de entidades de usuário.
     *
     * @param original - lista de dados de criação a serem convertidos
     * @return List - lista de entidades correspondentes
     */
    public List<UsuarioEntity> toEntityList(List<? extends UsuarioCreateDTO> original) {
        return original.stream().map(this::toEntity).toList();
    }

    /**
     * Converte uma entidade de usuário em seu DTO de resposta.
     *
     * @param entity - entidade a ser convertida
     * @return UsuarioResponseDTO - o DTO de resposta correspondente
     */
    public UsuarioResponseDTO toResponse(UsuarioEntity entity) {
        return new UsuarioResponseDTO(entity.getId(), entity.getNome(), entity.getEmail(), entity.getCargo());
    }

    /**
     * Converte uma lista de entidades de usuário em uma lista de DTOs de resposta.
     *
     * @param original - lista de entidades a serem convertidas
     * @return List - lista de DTOs de resposta correspondentes
     */
    public List<UsuarioResponseDTO> toResponseList(List<? extends UsuarioEntity> original) {
        return original.stream().map(this::toResponse).toList();
    }
}
