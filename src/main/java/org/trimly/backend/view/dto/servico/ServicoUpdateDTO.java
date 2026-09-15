package org.trimly.backend.view.dto.servico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.trimly.backend.model.entity.servico.ServicoStatus;

/**
 * Campos atualizáveis de um serviço. Todos são opcionais (semântica de PATCH); uma requisição com
 * todos nulos é um no-op.
 *
 * @param nome - novo nome
 * @param valor - novo valor
 * @param duracao - nova duração em minutos
 * @param status - novo status
 */
public record ServicoUpdateDTO(
        @Schema(description = "Opcional. Novo nome, único no catálogo", example = "Corte Degradê")
        String nome,

        @Schema(description = "Opcional. Novo preço, em reais", example = "45.00")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @Schema(description = "Opcional. Nova duração em minutos", example = "40")
        @Positive(message = "Duração deve ser positiva")
        Integer duracao,

        @Schema(description = "Opcional. Novo status no catálogo", example = "INATIVO")
        ServicoStatus status) {}
