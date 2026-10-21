package org.trimly.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Testes para {@link BackendApplication}.
 */
@SpringBootTest
class BackendApplicationTests {
    /**
     * Contexto da aplicação carregado pelo Spring.
     * @see {@link ApplicationContext}
     */
    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }
}
