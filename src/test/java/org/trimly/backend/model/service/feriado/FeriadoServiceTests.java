package org.trimly.backend.model.service.feriado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.exception.feriado.FeriadosIndisponiveisException;
import org.trimly.backend.model.integration.feriados.FeriadoResponseDTO;
import org.trimly.backend.model.integration.feriados.FeriadosClient;

/**
 * Testes para {@link FeriadoService}.
 */
@ExtendWith(MockitoExtension.class)
class FeriadoServiceTests {
    /**
     * Ano usado nos cenários.
     */
    private static final int ANO = 2026;

    /**
     * Cliente da BrasilAPI simulado.
     * @see {@link FeriadosClient}
     */
    @Mock
    private FeriadosClient client;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link FeriadoService}
     */
    @InjectMocks
    private FeriadoService service;

    @Test
    @DisplayName("Retorna cópia imutável dos feriados do cliente")
    void testeDadoClienteRespondendoQuandoListarEntaoRetornaCopiaImutavel() {
        List<FeriadoResponseDTO> feriados = new ArrayList<>(
                List.of(new FeriadoResponseDTO("2026-12-25", "Natal", "national"))
        );
        when(client.listarPorAno(ANO)).thenReturn(feriados);

        List<FeriadoResponseDTO> resultado = service.findByAno(ANO);

        assertEquals(feriados, resultado);
        assertThrows(UnsupportedOperationException.class, resultado::clear);
    }

    @Test
    @DisplayName("Converte falha do Feign em FeriadosIndisponiveisException com a causa")
    void testeDadoFalhaDoFeignQuandoListarEntaoLancaIndisponivelComCausa() {
        FeignException falha = mock(FeignException.class);
        when(client.listarPorAno(ANO)).thenThrow(falha);

        FeriadosIndisponiveisException exception = assertThrows(
                FeriadosIndisponiveisException.class,
                () -> service.findByAno(ANO)
        );

        assertSame(falha, exception.getCause());
        assertEquals("Feriados indisponíveis no momento", exception.getMessage());
    }
}
