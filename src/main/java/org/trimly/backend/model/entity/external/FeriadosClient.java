package org.trimly.backend.model.entity.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "brasilapi-feriados", url = "https://brasilapi.com.br/api/feriados/v1")
public interface FeriadosClient {

    @GetMapping("/{ano}")
    List<FeriadoDTO> listarPorAno(@PathVariable("ano") int ano);
}
