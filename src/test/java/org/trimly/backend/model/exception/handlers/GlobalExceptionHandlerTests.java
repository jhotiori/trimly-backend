package org.trimly.backend.model.exception.handlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Testes para {@link GlobalExceptionHandler}.
 */
@WebMvcTest(GlobalExceptionHandlerTests.ErroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTests.ErroController.class)
class GlobalExceptionHandlerTests {
    /**
     * Cliente HTTP simulado para exercitar o handler.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Junta as mensagens de validação em ordem alfabética e retorna 400")
    void testeDadoCorpoComCamposInvalidosQuandoValidarEntaoRetornaMensagensOrdenadas() throws Exception {
        mockMvc.perform(post("/erro/validacao").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Idade obrigatória; Nome obrigatório"));
    }

    @Test
    @DisplayName("Rejeita corpo mal formatado e retorna 400")
    void testeDadoCorpoMalFormatadoQuandoEnviarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(post("/erro/validacao").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou mal formatado"));
    }

    @Test
    @DisplayName("Rejeita corpo ausente e retorna 400")
    void testeDadoCorpoAusenteQuandoEnviarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(post("/erro/validacao").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou mal formatado"));
    }

    @Test
    @DisplayName("Rejeita parâmetro de tipo incompatível e retorna 400")
    void testeDadoParametroDeTipoIncompativelQuandoRequisitarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(get("/erro/numero/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro da requisição com valor inválido"));
    }

    @Test
    @DisplayName("Esconde o detalhe de falha inesperada e retorna 500")
    void testeDadoFalhaInesperadaQuandoLancadaEntaoRetornaErroInternoComMensagemGenerica() throws Exception {
        mockMvc.perform(get("/erro/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado ao processar a requisição"));
    }

    record Entrada(@NotBlank(message = "Nome obrigatório") String nome, @NotNull(message = "Idade obrigatória") Integer idade) {}

    @RestController
    static class ErroController {
        @PostMapping("/erro/validacao")
        void validar(@Valid @RequestBody Entrada entrada) {}

        @GetMapping("/erro/numero/{id}")
        void receberNumero(@PathVariable Long id) {}

        @GetMapping("/erro/inesperado")
        void lancarInesperado() {
            throw new IllegalStateException("detalhe interno");
        }
    }
}
