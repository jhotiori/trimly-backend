package org.trimly.backend.model.service.feriado;

import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trimly.backend.model.exception.feriado.FeriadosIndisponiveisException;
import org.trimly.backend.model.integration.feriados.FeriadoResponseDTO;
import org.trimly.backend.model.integration.feriados.FeriadosClient;

/**
 * Consulta os feriados nacionais na BrasilAPI.
 */
@Service
@RequiredArgsConstructor
public class FeriadoService {
    /**
     * Cliente da API de feriados nacionais (BrasilAPI), com cache por ano.
     * @see {@link FeriadosClient}
     */
    private final FeriadosClient client;

    /**
     * Lista os feriados nacionais do ano informado.
     *
     * @param ano - ano de referência
     * @throws FeriadosIndisponiveisException - quando a BrasilAPI falha ou não responde
     * @return List - feriados do ano, sem permitir alteração da lista em cache
     */
    public List<FeriadoResponseDTO> findByAno(int ano) {
        try {
            return List.copyOf(client.listarPorAno(ano));
        } catch (FeignException exception) {
            throw new FeriadosIndisponiveisException(exception);
        }
    }
}
