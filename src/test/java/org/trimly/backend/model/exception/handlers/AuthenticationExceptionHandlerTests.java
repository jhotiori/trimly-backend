package org.trimly.backend.model.exception.handlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.exception.auth.AuthException;

/**
 * Testes para {@link AuthenticationExceptionHandler}.
 */
@WebMvcTest(AuthenticationExceptionHandlerTests.ErroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthenticationExceptionHandlerTests.ErroController.class)
class AuthenticationExceptionHandlerTests {
    /**
     * Cliente HTTP simulado para exercitar o handler.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Converte credenciais inválidas em 401 com mensagem fixa")
    void testeDadoCredenciaisInvalidasQuandoLancadaEntaoRetornaNaoAutorizado() throws Exception {
        mockMvc.perform(get("/erro/credenciais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Converte qualquer falha de autenticação em 401")
    void testeDadoFalhaGenericaDeAutenticacaoQuandoLancadaEntaoRetornaNaoAutorizado() throws Exception {
        mockMvc.perform(get("/erro/generica"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token expirado"));
    }

    @RestController
    static class ErroController {
        @GetMapping("/erro/credenciais")
        void lancarCredenciaisInvalidas() {
            throw new AuthCredenciaisInvalidasException();
        }

        @GetMapping("/erro/generica")
        void lancarFalhaGenerica() {
            throw new AuthException("Token expirado");
        }
    }
}
