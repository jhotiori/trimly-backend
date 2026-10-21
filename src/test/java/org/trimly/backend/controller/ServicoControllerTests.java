package org.trimly.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

/**
 * Testes para {@link ServicoController}.
 */
@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ServicoMapper.class)
class ServicoControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/servicos";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de serviços simulado.
     * @see {@link ServicoService}
     */
    @MockitoBean
    private ServicoService service;

    @Test
    @DisplayName("Cria serviço válido e retorna 201")
    void testeDadoServicoValidoQuandoCriarEntaoRetornaCriado() throws Exception {
        ServicoCreateDTO request = new ServicoCreateDTO("Corte", new BigDecimal("40.00"), 30);
        when(service.create(request)).thenReturn(servico(1L, "Corte"));

        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Corte\",\"valor\":40.00,\"duracao\":30}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Corte"))
                .andExpect(jsonPath("$.duracao").value(30))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    @DisplayName("Rejeita criação com nome vazio e retorna 400")
    void testeDadoNomeVazioQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"valor\":40.00,\"duracao\":30}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome não pode ser vazio"));
    }

    @Test
    @DisplayName("Atualiza serviço existente e retorna 200")
    void testeDadoServicoExistenteQuandoAtualizarEntaoRetornaOk() throws Exception {
        ServicoUpdateDTO request = new ServicoUpdateDTO("Barba", null, null, null);
        when(service.update(1L, request)).thenReturn(servico(1L, "Barba"));

        mockMvc.perform(
                patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Barba\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Barba"));
    }

    @Test
    @DisplayName("Atualiza serviço inexistente e retorna 404")
    void testeDadoServicoInexistenteQuandoAtualizarEntaoRetornaNaoEncontrado() throws Exception {
        when(service.update(99L, new ServicoUpdateDTO("Barba", null, null, null)))
                .thenThrow(new EntityNotFoundException("Serviço não encontrado"));

        mockMvc.perform(
                patch(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Barba\"}")
        ).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    @DisplayName("Remove serviço existente e retorna 204")
    void testeDadoServicoExistenteQuandoDeletarEntaoRetornaSemConteudo() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("Remove serviço inexistente e retorna 404")
    void testeDadoServicoInexistenteQuandoDeletarEntaoRetornaNaoEncontrado() throws Exception {
        doThrow(new EntityNotFoundException("Serviço não encontrado")).when(service).deleteById(99L);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista todos os serviços e retorna 200")
    void testeDadoServicosCadastradosQuandoListarEntaoRetornaLista() throws Exception {
        when(service.findAll()).thenReturn(List.of(servico(1L, "Corte"), servico(2L, "Barba")));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nome").value("Barba"));
    }

    @Test
    @DisplayName("Busca serviço por id e retorna 200")
    void testeDadoServicoExistenteQuandoBuscarPorIdEntaoRetornaOk() throws Exception {
        when(service.findById(1L)).thenReturn(servico(1L, "Corte"));

        mockMvc.perform(get(ENDPOINT + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Corte"));
    }

    @Test
    @DisplayName("Busca serviço inexistente por id e retorna 404")
    void testeDadoServicoInexistenteQuandoBuscarPorIdEntaoRetornaNaoEncontrado() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Serviço não encontrado"));

        mockMvc.perform(get(ENDPOINT + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Lista serviços por status e retorna 200")
    void testeDadoStatusValidoQuandoBuscarPorStatusEntaoRetornaLista() throws Exception {
        when(service.findByStatus(ServicoStatus.ATIVO)).thenReturn(List.of(servico(1L, "Corte")));

        mockMvc.perform(get(ENDPOINT + "/status/ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ATIVO"));
    }

    @Test
    @DisplayName("Rejeita status inexistente na busca e retorna 400")
    void testeDadoStatusInvalidoQuandoBuscarPorStatusEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/status/INEXISTENTE")).andExpect(status().isBadRequest());
    }

    private static ServicoEntity servico(Long id, String nome) {
        return ServicoEntity.builder()
                .id(id)
                .nome(nome)
                .valor(new BigDecimal("40.00"))
                .duracao(30)
                .status(ServicoStatus.ATIVO)
                .build();
    }
}
