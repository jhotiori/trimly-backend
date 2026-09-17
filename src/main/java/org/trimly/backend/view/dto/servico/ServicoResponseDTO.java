package org.trimly.backend.view.dto.servico;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.trimly.backend.model.entity.servico.ServicoStatus;

/**
 * Representação de resposta de um serviço.
 *
 * @param id - identificador do serviço
 * @param nome - nome do serviço
 * @param valor - valor cobrado pelo serviço
 * @param duracao - duração do serviço em minutos
 * @param status - status atual do serviço
 */
public record ServicoResponseDTO(
        @Schema(description = "Identificador do serviço", example = "1") Long id,

        @Schema(description = "Nome do serviço, único no catálogo", example = "Corte Masculino") String nome,

        @Schema(description = "Preço cobrado, em reais", example = "40.00") BigDecimal valor,

        @Schema(description = "Duração do atendimento em minutos", example = "30") Integer duracao,

        @Schema(
                description = "Status no catálogo; só ATIVO aceita novos agendamentos",
                example = "ATIVO"
        ) ServicoStatus status
) {}
