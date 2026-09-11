package org.trimly.backend.model.repository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;

/**
 * Monta a {@link Specification} que consulta {@link AgendamentoEntity} a partir dos campos preenchidos
 * de um {@link AgendamentoFilter}.
 */
public class AgendamentoSpecification {
    /**
     * Monta uma especificação que aplica apenas os filtros preenchidos.
     *
     * Cada campo nulo do filtro é ignorado; quando o filtro traz uma data, a
     * restrição cobre o dia inteiro, do início até o começo do dia seguinte.
     *
     * @param filtro - critérios de status, data, usuário e serviço, com campos opcionais
     * @return Specification - predicado combinando os filtros preenchidos
     */
    public static Specification<AgendamentoEntity> comFiltros(AgendamentoFilter filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.status() != null) {
                predicates.add(cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.data() != null) {
                LocalDateTime inicioDoDia = filtro.data().atStartOfDay();
                LocalDateTime inicioDoProximoDia = filtro.data().plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), inicioDoDia));
                predicates.add(cb.lessThan(root.get("data"), inicioDoProximoDia));
            }
            if (filtro.usuarioId() != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), filtro.usuarioId()));
            }
            if (filtro.servicoId() != null) {
                predicates.add(cb.equal(root.get("servico").get("id"), filtro.servicoId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
