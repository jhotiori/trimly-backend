package org.trimly.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.entity.ServicoEntity;
import org.trimly.backend.entity.enums.StatusServico;

public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {
    List<ServicoEntity> findByNomeContainingIgnoreCase(String nome); // Não diferencia maiúsculas/minúsculas
    List<ServicoEntity> findByStatus(StatusServico status);
}
