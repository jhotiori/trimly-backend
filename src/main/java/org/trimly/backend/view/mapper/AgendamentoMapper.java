package org.trimly.backend.view.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoResponseDTO;

/**
 * Mapper de agendamentos. Converte à mão entre {@code AgendamentoEntity} e seus DTOs, sem MapStruct.
 */
@Component
public class AgendamentoMapper {
    /**
     * Converte os dados de criação em uma entidade de agendamento com status agendado.
     *
     * @param request - dados de criação do agendamento
     * @param usuario - usuário dono do agendamento
     * @param servico - serviço agendado
     * @return AgendamentoEntity - a entidade preenchida a partir dos dados informados
     */
    public AgendamentoEntity toEntity(AgendamentoCreateDTO request, UsuarioEntity usuario, ServicoEntity servico) {
        AgendamentoEntity entity = new AgendamentoEntity();

        entity.setData(LocalDateTime.of(request.data(), request.horario()));
        entity.setDuracao(servico.getDuracao());
        entity.setStatus(AgendamentoStatus.AGENDADO);
        entity.setUsuario(usuario);
        entity.setServico(servico);

        return entity;
    }

    /**
     * Converte uma entidade de agendamento em seu DTO de resposta.
     *
     * @param entity - entidade a ser convertida
     * @return AgendamentoResponseDTO - o DTO de resposta correspondente
     */
    public AgendamentoResponseDTO toResponse(AgendamentoEntity entity) {
        return new AgendamentoResponseDTO(
                entity.getId(),
                entity.getData(),
                entity.getDuracao(),
                entity.getStatus(),
                entity.getUsuario().getId(),
                entity.getServico().getId());
    }

    /**
     * Converte uma lista de entidades de agendamento em uma lista de DTOs de resposta.
     *
     * @param original - lista de entidades a serem convertidas
     * @return List - lista de DTOs de resposta correspondentes
     */
    public List<AgendamentoResponseDTO> toResponseList(List<? extends AgendamentoEntity> original) {
        return original.stream().map(this::toResponse).toList();
    }
}
