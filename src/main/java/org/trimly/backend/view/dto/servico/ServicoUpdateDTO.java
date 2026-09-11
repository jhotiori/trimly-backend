package org.trimly.backend.view.dto.servico;

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
        String nome,
        @Positive(message = "Valor deve ser positivo") BigDecimal valor,
        @Positive(message = "Duração deve ser positiva") Integer duracao,
        ServicoStatus status) {}
