package org.trimly.backend.view.dto.servico;

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
public record ServicoResponseDTO(Long id, String nome, BigDecimal valor, Integer duracao, ServicoStatus status) {}
