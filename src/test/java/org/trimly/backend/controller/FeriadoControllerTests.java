package org.trimly.backend.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import feign.FeignException;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.exception.feriado.FeriadosIndisponiveisException;
import org.trimly.backend.model.integration.feriados.FeriadoResponseDTO;
import org.trimly.backend.model.service.feriado.FeriadoService;

/**
 * Testes para {@link FeriadoController}.
 */
@WebMvcTest(FeriadoController.class)
@AutoConfigureMockMvc(addFilters = false)
class FeriadoControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/feriados";

    /**
     * Feriado devolvido pelo serviço simulado.
     */
    private static final FeriadoResponseDTO NATAL = new FeriadoResponseDTO("2027-12-25", "Natal", "national");

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de feriados simulado.
     * @see {@link FeriadoService}
     */
    @MockitoBean
    private FeriadoService service;

    @Test
    @DisplayName("Lista feriados do ano atual quando o ano é omitido e retorna 200")
    void testeDadoAnoAusenteQuandoListarEntaoUsaAnoAtual() throws Exception {
        when(service.findByAno(Year.now().getValue())).thenReturn(List.of(NATAL));

        mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Natal"));
    }

    @Test
    @DisplayName("Lista feriados do ano informado e retorna 200")
    void testeDadoAnoValidoQuandoListarEntaoRetornaOk() throws Exception {
        when(service.findByAno(2027)).thenReturn(List.of(NATAL));

        mockMvc.perform(get(ENDPOINT).param("ano", "2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2027-12-25"));
    }

    @Test
    @DisplayName("Rejeita ano fora do intervalo e retorna 400")
    void testeDadoAnoForaDoIntervaloQuandoListarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(get(ENDPOINT).param("ano", "1800")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rejeita ano não numérico e retorna 400")
    void testeDadoAnoNaoNumericoQuandoListarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(get(ENDPOINT).param("ano", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retorna 503 quando a BrasilAPI está indisponível")
    void testeDadoBrasilApiIndisponivelQuandoListarEntaoRetornaServicoIndisponivel() throws Exception {
        when(service.findByAno(2027)).thenThrow(new FeriadosIndisponiveisException(mock(FeignException.class)));

        mockMvc.perform(get(ENDPOINT).param("ano", "2027"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Feriados indisponíveis no momento"));
    }
}
