package org.trimly.backend.model.service.recesso;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNoPassadoException;
import org.trimly.backend.model.exception.recesso.RecessoPeriodoInvalidoException;
import org.trimly.backend.model.repository.RecessoRepository;

/**
 * Valida datas fora do passado, fim não anterior ao início e ausência de sobreposição entre recessos.
 */
@Component
@RequiredArgsConstructor
public class RecessoValidator {
    /**
     * Repositório de recessos, usado para checar sobreposição de períodos.
     * @see {@link RecessoRepository}
     */
    private final RecessoRepository repository;

    /**
     * Verifica se a data informada não é anterior a hoje.
     *
     * @param data - data de início ou de fim do recesso
     * @throws RecessoNoPassadoException - quando a data é anterior a hoje
     */
    public void validateNoPassado(LocalDate data) {
        if (data.isBefore(LocalDate.now())) {
            throw new RecessoNoPassadoException();
        }
    }

    /**
     * Verifica se a data de fim não é anterior à data de início.
     *
     * @param dataInicio - primeiro dia do recesso
     * @param dataFim - último dia do recesso
     * @throws RecessoPeriodoInvalidoException - quando o fim é anterior ao início
     */
    public void validatePeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            throw new RecessoPeriodoInvalidoException();
        }
    }

    /**
     * Verifica se o período informado se sobrepõe a outro recesso, com as duas pontas inclusivas.
     *
     * @param id - identificador do recesso a ignorar na verificação, ou nulo na criação
     * @param dataInicio - primeiro dia do novo período
     * @param dataFim - último dia do novo período
     * @throws RecessoConflitoException - quando o período se sobrepõe a outro recesso
     */
    public void validateConflito(Long id, LocalDate dataInicio, LocalDate dataFim) {
        List<RecessoEntity> recessosSobrepostos = repository
                .findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(dataFim, dataInicio);

        boolean existeConflito = recessosSobrepostos.stream()
                .anyMatch(recessoExistente -> !recessoExistente.getId().equals(id));

        if (existeConflito) {
            throw new RecessoConflitoException();
        }
    }
}
