package org.trimly.backend.model.integration.feriados;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para a API pública de feriados nacionais da BrasilAPI.
 */
@FeignClient(name = "brasilapi-feriados", url = "https://brasilapi.com.br/api/feriados/v1")
public interface FeriadosClient {
    /**
     * Lista os feriados nacionais de um ano.
     *
     * @param ano - ano de referência
     * @return lista de feriados nacionais do ano informado
     */
    @GetMapping("/{ano}")
    List<FeriadoResponseDTO> listarPorAno(@PathVariable("ano") int ano);
}
