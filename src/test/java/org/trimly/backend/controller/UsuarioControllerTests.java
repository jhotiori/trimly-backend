package org.trimly.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.usuario.UsuarioNaoEncontradoException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

/**
 * Testes para {@link UsuarioController}.
 */
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UsuarioMapper.class)
class UsuarioControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/usuarios";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de usuários simulado.
     * @see {@link UsuarioService}
     */
    @MockitoBean
    private UsuarioService service;

    @Test
    @DisplayName("Atualiza usuário existente e retorna 200")
    void testeDadoUsuarioExistenteQuandoAtualizarEntaoRetornaOk() throws Exception {
        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Ana Lima", null, null, null);
        when(service.update(1L, request)).thenReturn(usuario(1L, "Ana Lima", "ana@trimly.com"));

        mockMvc.perform(
                patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Ana Lima\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana Lima"));
    }

    @Test
    @DisplayName("Atualiza usuário inexistente e retorna 404")
    void testeDadoUsuarioInexistenteQuandoAtualizarEntaoRetornaNaoEncontrado() throws Exception {
        when(service.update(99L, new UsuarioUpdateDTO("Ana Lima", null, null, null)))
                .thenThrow(new UsuarioNaoEncontradoException());

        mockMvc.perform(
                patch(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Ana Lima\"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Remove usuário existente e retorna 204")
    void testeDadoUsuarioExistenteQuandoDeletarEntaoRetornaSemConteudo() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("Remove usuário inexistente e retorna 404")
    void testeDadoUsuarioInexistenteQuandoDeletarEntaoRetornaNaoEncontrado() throws Exception {
        doThrow(new UsuarioNaoEncontradoException()).when(service).deleteById(99L);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista todos os usuários e retorna 200")
    void testeDadoUsuariosCadastradosQuandoListarEntaoRetornaLista() throws Exception {
        when(service.findAll())
                .thenReturn(List.of(usuario(1L, "Ana", "ana@trimly.com"), usuario(2L, "Beto", "beto@trimly.com")));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nome").value("Beto"));
    }

    @Test
    @DisplayName("Busca usuário por id e retorna 200")
    void testeDadoUsuarioExistenteQuandoBuscarPorIdEntaoRetornaOk() throws Exception {
        when(service.findById(1L)).thenReturn(usuario(1L, "Ana", "ana@trimly.com"));

        mockMvc.perform(get(ENDPOINT + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    @DisplayName("Busca usuário inexistente por id e retorna 404")
    void testeDadoUsuarioInexistenteQuandoBuscarPorIdEntaoRetornaNaoEncontrado() throws Exception {
        when(service.findById(99L)).thenThrow(new UsuarioNaoEncontradoException());

        mockMvc.perform(get(ENDPOINT + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    private static UsuarioEntity usuario(Long id, String nome, String email) {
        return UsuarioEntity.builder().id(id).nome(nome).email(email).senha("hash").cargo(UsuarioCargo.CLIENTE).build();
    }
}
