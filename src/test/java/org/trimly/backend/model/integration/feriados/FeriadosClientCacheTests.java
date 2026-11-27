package org.trimly.backend.model.integration.feriados;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.trimly.backend.infra.CacheConfig;

/**
 * Testes do cache por ano em {@link FeriadosClient}.
 */
@SpringJUnitConfig({CacheConfig.class, FeriadosClientCacheTests.ClientConfig.class})
class FeriadosClientCacheTests {
    /**
     * Ano usado nos cenários.
     */
    private static final int ANO = 2026;

    /**
     * Cliente simulado, envolvido pelo proxy de cache.
     * @see {@link FeriadosClient}
     */
    @Autowired
    private FeriadosClient client;

    /**
     * Gerenciador de cache, limpo entre os cenários.
     * @see {@link CacheManager}
     */
    @Autowired
    private CacheManager cacheManager;

    /**
     * Mock por trás do proxy de cache, para contar as chamadas reais.
     * @see {@link FeriadosClient}
     */
    @Autowired
    private ClientConfig config;

    @AfterEach
    void limpar() {
        cacheManager.getCache(CacheConfig.FERIADOS_CACHE).clear();
        reset(config.mock);
    }

    @Test
    @DisplayName("Segunda consulta do mesmo ano não chama a BrasilAPI")
    void testeDadoAnoConsultadoQuandoConsultarDeNovoEntaoUsaCache() {
        when(config.mock.listarPorAno(ANO)).thenReturn(List.of(new FeriadoResponseDTO("2026-12-25", "Natal", "national")));

        client.listarPorAno(ANO);
        client.listarPorAno(ANO);

        verify(config.mock, times(1)).listarPorAno(ANO);
    }

    @Test
    @DisplayName("Falha da BrasilAPI não fica em cache")
    void testeDadoFalhaQuandoConsultarDeNovoEntaoChamaNovamente() {
        when(config.mock.listarPorAno(ANO)).thenThrow(new IllegalStateException("indisponível"));

        assertThrows(IllegalStateException.class, () -> client.listarPorAno(ANO));
        assertThrows(IllegalStateException.class, () -> client.listarPorAno(ANO));

        verify(config.mock, times(2)).listarPorAno(ANO);
    }

    /**
     * Registra o mock do cliente como bean, para receber o proxy de cache.
     */
    @Configuration
    static class ClientConfig {
        /**
         * Mock do cliente registrado como bean.
         * @see {@link FeriadosClient}
         */
        private final FeriadosClient mock = mock(FeriadosClient.class);

        @Bean
        FeriadosClient feriadosClient() {
            return mock;
        }
    }
}
