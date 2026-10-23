package org.trimly.backend.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;

/**
 * Repositório de serviços.
 */
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {
    /**
     * Indica se já existe um serviço com o nome informado, ignorando maiúsculas e minúsculas.
     *
     * @param nome - nome do serviço a ser verificado
     * @return boolean - `true` quando já há um serviço com esse nome
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Retorna os serviços cujo status é igual ao informado.
     *
     * @param status - status usado no filtro
     * @return List - serviços com esse status, vazia quando não houver nenhum
     */
    List<ServicoEntity> findByStatus(ServicoStatus status);
}
