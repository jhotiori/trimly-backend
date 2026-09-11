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

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.ServicoEntity;
import org.trimly.backend.model.entity.enums.ServicoStatus;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoResponseDTO;
import org.trimly.backend.view.mapper.ServicoMapper;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServicoControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServicoService service;

    @MockitoBean
    private ServicoMapper mapper;

    /**
     * Verifica que criar um serviço com um corpo válido retorna 201.
     */
    @Test
    void servico_TestCreateComCorpoValidoRetorna201() throws Exception {
        ServicoCreateDTO request = new ServicoCreateDTO("Corte", BigDecimal.TEN, 30);
        ServicoEntity entity = new ServicoEntity();
        ServicoResponseDTO response = new ServicoResponseDTO(1L, "Corte", BigDecimal.TEN, 30, ServicoStatus.ATIVO);

        when(service.create(any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um serviço com nome em branco:
     * {@code GlobalExceptionHandler} intercepta a {@code MethodArgumentNotValidException} da validação de bean
     * antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void servico_TestCreateComNomeEmBrancoRetorna500() throws Exception {
        String corpo = """
                {"nome":"","valor":10,"duracao":30}""";

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar um serviço com valor e duração não positivos:
     * a mesma interceptação da {@code MethodArgumentNotValidException} retorna 500 em vez de 400.
     */
    @Test
    void servico_TestCreateComValorEDuracaoNaoPositivosRetorna500() throws Exception {
        String corpo = """
                {"nome":"Corte","valor":-10,"duracao":0}""";

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que atualizar um serviço existente com um corpo válido retorna 200.
     */
    @Test
    void servico_TestUpdateComCorpoValidoRetorna200() throws Exception {
        ServicoEntity entity = new ServicoEntity();
        ServicoResponseDTO response = new ServicoResponseDTO(1L, "Corte Novo", BigDecimal.TEN, 30, ServicoStatus.ATIVO);

        when(service.update(anyLong(), any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(patch("/api/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte Novo"}"""))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que atualizar um serviço para um nome já utilizado por outro retorna 409.
     */
    @Test
    void servico_TestUpdateComNomeDuplicadoRetorna409() throws Exception {
        when(service.update(anyLong(), any()))
                .thenThrow(new ServicoNomeDuplicadoException("Já existe um serviço com esse nome"));

        mockMvc.perform(patch("/api/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte Existente"}"""))
                .andExpect(status().isConflict());
    }

    /**
     * Verifica que listar os serviços cadastrados retorna 200.
     */
    @Test
    void servico_TestFindAllRetorna200() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/servicos")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um serviço existente pelo id retorna 200.
     */
    @Test
    void servico_TestFindByIdComIdExistenteRetorna200() throws Exception {
        ServicoEntity entity = new ServicoEntity();
        ServicoResponseDTO response = new ServicoResponseDTO(1L, "Corte", BigDecimal.TEN, 30, ServicoStatus.ATIVO);

        when(service.findById(1L)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/servicos/1")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar um serviço inexistente pelo id retorna 404.
     */
    @Test
    void servico_TestFindByIdComIdInexistenteRetorna404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Serviço não encontrado"));

        mockMvc.perform(get("/api/servicos/99")).andExpect(status().isNotFound());
    }

    /**
     * Verifica que listar os serviços com um status válido retorna 200.
     */
    @Test
    void servico_TestFindByStatusComStatusValidoRetorna200() throws Exception {
        when(service.findByStatus(ServicoStatus.ATIVO)).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/servicos/status/ATIVO")).andExpect(status().isOk());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para um segmento de status inválido: a
     * {@code MethodArgumentTypeMismatchException} da conversão do path variable é interceptada pelo
     * {@code GlobalExceptionHandler} antes do mapeamento padrão para 400, retornando 500.
     */
    @Test
    void servico_TestFindByStatusComStatusInvalidoRetorna500() throws Exception {
        mockMvc.perform(get("/api/servicos/status/NAO_EXISTE")).andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que remover um serviço existente retorna 204.
     */
    @Test
    void servico_TestDeleteByIdComIdExistenteRetorna204() throws Exception {
        mockMvc.perform(delete("/api/servicos/1")).andExpect(status().isNoContent());
    }

    /**
     * Verifica que remover um serviço com agendamento pendente retorna 409.
     */
    @Test
    void servico_TestDeleteByIdComAgendamentoPendenteRetorna409() throws Exception {
        doThrow(new ServicoComAgendamentoPendenteException(
                        "Não é possível remover um serviço com agendamento pendente"))
                .when(service)
                .deleteById(1L);

        mockMvc.perform(delete("/api/servicos/1")).andExpect(status().isConflict());
    }
}
