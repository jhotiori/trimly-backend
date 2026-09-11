package org.trimly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.UsuarioCargo;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService service;

    @MockitoBean
    private UsuarioMapper mapper;

    /**
     * Verifica que criar um usuário com um corpo válido retorna 201.
     */
    @Test
    void usuario_TestCreateComCorpoValidoRetorna201() throws Exception {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Nome", "email@trimly.com", "senha123");
        UsuarioEntity entity = new UsuarioEntity();
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Nome", "email@trimly.com", UsuarioCargo.CLIENTE);

        when(service.create(any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um usuário com e-mail em formato inválido:
     * {@code GlobalExceptionHandler} intercepta a {@code MethodArgumentNotValidException} da validação de bean
     * antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void usuario_TestCreateComEmailInvalidoRetorna500() throws Exception {
        String corpo = """
                {"nome":"Nome","email":"nao-e-um-email","senha":"senha123"}""";

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um usuário com senha em branco: a mesma
     * interceptação da {@code MethodArgumentNotValidException} retorna 500 em vez de 400.
     */
    @Test
    void usuario_TestCreateComSenhaEmBrancoRetorna500() throws Exception {
        String corpo = """
                {"nome":"Nome","email":"email@trimly.com","senha":""}""";

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que atualizar um usuário existente com um corpo válido retorna 200.
     */
    @Test
    void usuario_TestUpdateComCorpoValidoRetorna200() throws Exception {
        UsuarioEntity entity = new UsuarioEntity();
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Nome Novo", "email@trimly.com", UsuarioCargo.CLIENTE);

        when(service.update(anyLong(), any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(patch("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome Novo"}"""))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que atualizar um usuário para um e-mail já utilizado por outro retorna 409.
     */
    @Test
    void usuario_TestUpdateComEmailDuplicadoRetorna409() throws Exception {
        when(service.update(anyLong(), any()))
                .thenThrow(new UsuarioEmailExistenteException("Já existe um usuário com esse e-mail"));

        mockMvc.perform(patch("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"existente@trimly.com"}"""))
                .andExpect(status().isConflict());
    }

    /**
     * Verifica que listar os usuários cadastrados retorna 200.
     */
    @Test
    void usuario_TestFindAllRetorna200() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um usuário existente pelo id retorna 200.
     */
    @Test
    void usuario_TestFindByIdComIdExistenteRetorna200() throws Exception {
        UsuarioEntity entity = new UsuarioEntity();
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Nome", "email@trimly.com", UsuarioCargo.CLIENTE);

        when(service.findById(1L)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/usuarios/1")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um usuário inexistente pelo id retorna 404.
     */
    @Test
    void usuario_TestFindByIdComIdInexistenteRetorna404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Usuario não foi encontrado"));

        mockMvc.perform(get("/api/usuarios/99")).andExpect(status().isNotFound());
    }

    /**
     * Verifica que remover um usuário existente retorna 204.
     */
    @Test
    void usuario_TestDeleteByIdComIdExistenteRetorna204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
    }

    /**
     * Verifica que remover um usuário com agendamento pendente retorna 409.
     */
    @Test
    void usuario_TestDeleteByIdComAgendamentoPendenteRetorna409() throws Exception {
        doThrow(new UsuarioComAgendamentoPendenteException(
                        "Não é possível remover um usuário com agendamento pendente"))
                .when(service)
                .deleteById(1L);

        mockMvc.perform(delete("/api/usuarios/1")).andExpect(status().isConflict());
    }
}
