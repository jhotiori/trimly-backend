package org.trimly.backend.model.entity.servico;

/**
 * Status de um serviço no catálogo. {@code ATIVO} pode ser agendado; {@code INATIVO} fica fora da
 * oferta e não aceita novos agendamentos.
 */
public enum ServicoStatus {
    ATIVO,
    INATIVO
}
