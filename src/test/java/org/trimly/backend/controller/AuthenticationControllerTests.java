package org.trimly.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.service.auth.AuthenticationService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Testes para {@link AuthenticationController}.
 */
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/auth";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de autenticação simulado.
     * @see {@link AuthenticationService}
     */
    @MockitoBean
    private AuthenticationService service;

    @Test
    @DisplayName("Registra usuário válido e retorna 201 com o token")
    void testeDadoUsuarioValidoQuandoRegistrarEntaoRetornaCriadoComToken() throws Exception {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123456");
        when(service.register(request)).thenReturn(new AuthResponseDTO("token-jwt"));

        mockMvc.perform(
                post(ENDPOINT + "/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@trimly.com\",\"senha\":\"123456\"}")
        ).andExpect(status().isCreated()).andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    @DisplayName("Rejeita registro com senha curta e retorna 400")
    void testeDadoSenhaCurtaQuandoRegistrarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT + "/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@trimly.com\",\"senha\":\"123\"}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Senha deve ter no mínimo 6 caracteres"));
    }

    @Test
    @DisplayName("Autentica credenciais corretas e retorna 200 com o token")
    void testeDadoCredenciaisCorretasQuandoLogarEntaoRetornaOkComToken() throws Exception {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO("ana@trimly.com", "123456");
        when(service.login(request)).thenReturn(new AuthResponseDTO("token-jwt"));

        mockMvc.perform(
                post(ENDPOINT + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@trimly.com\",\"senha\":\"123456\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    @DisplayName("Recusa credenciais incorretas e retorna 401")
    void testeDadoCredenciaisIncorretasQuandoLogarEntaoRetornaNaoAutorizado() throws Exception {
        when(service.login(new AuthLoginRequestDTO("ana@trimly.com", "errada")))
                .thenThrow(new AuthCredenciaisInvalidasException());

        mockMvc.perform(
                post(ENDPOINT + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@trimly.com\",\"senha\":\"errada\"}")
        ).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Rejeita login sem e-mail e retorna 400")
    void testeDadoEmailVazioQuandoLogarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"senha\":\"123456\"}")
        ).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Email não pode ser vazio"));
    }
}
