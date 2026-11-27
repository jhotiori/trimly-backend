package org.trimly.backend.infra;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache em memória dos feriados nacionais por ano.
 *
 * Os feriados de um ano não mudam, então as entradas nunca expiram; o intervalo de anos aceito limita o tamanho.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Nome do cache de feriados, com o ano como chave.
     */
    public static final String FERIADOS_CACHE = "feriados";

    /**
     * Cria o gerenciador de cache em memória com o cache de feriados.
     *
     * @return CacheManager - gerenciador com o cache `feriados`
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(FERIADOS_CACHE);
    }
}
