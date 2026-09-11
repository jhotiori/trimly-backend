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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.AgendamentoEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusException;
import org.trimly.backend.model.service.agendamento.AgendamentoService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoResponseDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AgendamentoControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgendamentoService service;

    @MockitoBean
    private AgendamentoMapper mapper;

    /**
     * Verifica que criar um agendamento com um corpo válido retorna 201.
     */
    @Test
    void agendamento_TestCreateComCorpoValidoRetorna201() throws Exception {
        AgendamentoCreateDTO request =
                new AgendamentoCreateDTO(LocalDate.now().plusDays(1), LocalTime.of(10, 0), 1L, 2L);
        AgendamentoEntity entity = new AgendamentoEntity();
        AgendamentoResponseDTO response =
                new AgendamentoResponseDTO(1L, LocalDateTime.now().plusDays(1), 30, AgendamentoStatus.AGENDADO, 1L, 2L);

        when(service.create(any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um agendamento com uma data no passado:
     * {@code GlobalExceptionHandler} intercepta a {@code MethodArgumentNotValidException} da validação de bean
     * antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void agendamento_TestCreateComDataNoPassadoRetorna500() throws Exception {
        String corpo = """
                {"data":"2020-01-01","horario":"10:00:00","usuarioId":1,"servicoId":2}""";

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um agendamento com identificadores de usuário
     * e serviço não positivos: a mesma interceptação da {@code MethodArgumentNotValidException} retorna 500 em
     * vez de 400.
     */
    @Test
    void agendamento_TestCreateComIdsNaoPositivosRetorna500() throws Exception {
        String corpo = """
                {"data":"%s","horario":"10:00:00","usuarioId":-1,"servicoId":0}""".formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para um corpo de requisição com JSON malformado: a
     * {@code HttpMessageNotReadableException} da desserialização é interceptada pelo
     * {@code GlobalExceptionHandler} antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void agendamento_TestCreateComJsonMalformadoRetorna500() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que atualizar um agendamento existente com um corpo válido retorna 200.
     */
    @Test
    void agendamento_TestUpdateComCorpoValidoRetorna200() throws Exception {
        AgendamentoEntity entity = new AgendamentoEntity();
        AgendamentoResponseDTO response = new AgendamentoResponseDTO(
                1L, LocalDateTime.now().plusDays(1), 30, AgendamentoStatus.CANCELADO, 1L, 2L);

        when(service.update(anyLong(), any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CANCELADO"}"""))
                .andExpect(status().isOk());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para uma transição de status inválida:
     * {@code AgendamentoStatusException} cai no handler genérico de {@code TrimlyException} em
     * {@code DomainExceptionHandler}, que retorna 500 em vez do 422 documentado.
     */
    @Test
    void agendamento_TestUpdateComTransicaoDeStatusInvalidaRetorna500() throws Exception {
        when(service.update(anyLong(), any()))
                .thenThrow(new AgendamentoStatusException("O agendamento não pode ser alterado neste status"));

        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONCLUIDO"}"""))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para um literal de status inválido: a
     * {@code HttpMessageNotReadableException} da desserialização do enum é interceptada pelo
     * {@code GlobalExceptionHandler} antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void agendamento_TestUpdateComStatusInvalidoRetorna500() throws Exception {
        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"NAO_EXISTE"}"""))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que listar os agendamentos sem nenhum filtro retorna 200.
     */
    @Test
    void agendamento_TestFindAllSemFiltrosRetorna200() throws Exception {
        when(service.findAll(any())).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/agendamentos")).andExpect(status().isOk());
    }

    /**
     * Verifica que listar os agendamentos com filtros informados por query param retorna 200.
     */
    @Test
    void agendamento_TestFindAllComFiltrosRetorna200() throws Exception {
        when(service.findAll(any())).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/agendamentos")
                        .param("status", "AGENDADO")
                        .param("data", LocalDate.now().plusDays(1).toString())
                        .param("usuarioId", "1")
                        .param("servicoId", "2"))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um agendamento existente pelo id retorna 200.
     */
    @Test
    void agendamento_TestFindByIdComIdExistenteRetorna200() throws Exception {
        AgendamentoEntity entity = new AgendamentoEntity();
        AgendamentoResponseDTO response =
                new AgendamentoResponseDTO(1L, LocalDateTime.now().plusDays(1), 30, AgendamentoStatus.AGENDADO, 1L, 2L);

        when(service.findById(1L)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/agendamentos/1")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um agendamento inexistente pelo id retorna 404.
     */
    @Test
    void agendamento_TestFindByIdComIdInexistenteRetorna404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Agendamento não foi encontrado"));

        mockMvc.perform(get("/api/agendamentos/99")).andExpect(status().isNotFound());
    }

    /**
     * Verifica que remover um agendamento existente retorna 204.
     */
    @Test
    void agendamento_TestDeleteByIdComIdExistenteRetorna204() throws Exception {
        mockMvc.perform(delete("/api/agendamentos/1")).andExpect(status().isNoContent());
    }

    /**
     * Verifica que remover um agendamento inexistente retorna 404.
     */
    @Test
    void agendamento_TestDeleteByIdComIdInexistenteRetorna404() throws Exception {
        doThrow(new EntityNotFoundException("Agendamento não foi encontrado"))
                .when(service)
                .deleteById(99L);

        mockMvc.perform(delete("/api/agendamentos/99")).andExpect(status().isNotFound());
    }
}
