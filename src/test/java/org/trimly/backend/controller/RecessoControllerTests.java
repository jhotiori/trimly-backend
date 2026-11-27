package org.trimly.backend.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNaoEncontradoException;
import org.trimly.backend.model.exception.recesso.RecessoNoPassadoException;
import org.trimly.backend.model.exception.recesso.RecessoPeriodoInvalidoException;
import org.trimly.backend.model.service.recesso.RecessoService;
import org.trimly.backend.view.dto.recesso.RecessoCreateDTO;
import org.trimly.backend.view.dto.recesso.RecessoUpdateDTO;
import org.trimly.backend.view.mapper.RecessoMapper;

/**
 * Testes para {@link RecessoController}.
 */
@WebMvcTest(RecessoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RecessoMapper.class)
class RecessoControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/recessos";

    /**
     * Primeiro dia do recesso usado nos cenários.
     */
    private static final LocalDate INICIO = LocalDate.of(2030, 12, 1);

    /**
     * Último dia do recesso usado nos cenários.
     */
    private static final LocalDate FIM = LocalDate.of(2030, 12, 17);

    /**
     * Regra exigida nos endpoints de escrita.
     */
    private static final String REGRA_GESTOR = "hasAnyRole('DONO', 'ADMIN')";

    /**
     * Corpo JSON de criação válido.
     */
    private static final String CORPO_VALIDO = "{\"descricao\":\"Férias da Barbearia\",\"dataInicio\":\"2030-12-01\","
            + "\"dataFim\":\"2030-12-17\"}";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de recessos simulado.
     * @see {@link RecessoService}
     */
    @MockitoBean
    private RecessoService service;

    @Test
    @DisplayName("Cria recesso válido e retorna 201")
    void testeDadoRecessoValidoQuandoCriarEntaoRetornaCriado() throws Exception {
        RecessoCreateDTO request = new RecessoCreateDTO("Férias da Barbearia", INICIO, FIM);
        when(service.create(request)).thenReturn(recesso(1L));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Férias da Barbearia"))
                .andExpect(jsonPath("$.dataInicio").value("2030-12-01"))
                .andExpect(jsonPath("$.dataFim").value("2030-12-17"));
    }

    @Test
    @DisplayName("Rejeita criação sem descrição e retorna 400")
    void testeDadoDescricaoVaziaQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\" \",\"dataInicio\":\"2030-12-01\",\"dataFim\":\"2030-12-17\"}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Descrição não pode ser vazia"));
    }

    @Test
    @DisplayName("Rejeita criação sem datas e retorna 400")
    void testeDadoDatasNulasQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"Férias\"}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Data de fim não pode ser nula; Data de início não pode ser nula")
                );
    }

    @Test
    @DisplayName("Rejeita data fora do formato yyyy-MM-dd e retorna 400")
    void testeDadoDataMalFormatadaQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Férias\",\"dataInicio\":\"01/12/2030\",\"dataFim\":\"2030-12-17\"}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retorna 422 quando o início está no passado")
    void testeDadoInicioNoPassadoQuandoCriarEntaoRetornaNaoProcessavel() throws Exception {
        when(service.create(new RecessoCreateDTO("Férias da Barbearia", INICIO, FIM)))
                .thenThrow(new RecessoNoPassadoException());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Recesso não pode ser no passado"));
    }

    @Test
    @DisplayName("Retorna 422 quando o fim é anterior ao início")
    void testeDadoFimAntesDoInicioQuandoCriarEntaoRetornaNaoProcessavel() throws Exception {
        when(service.create(new RecessoCreateDTO("Férias da Barbearia", INICIO, FIM)))
                .thenThrow(new RecessoPeriodoInvalidoException());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Data de fim deve ser igual ou posterior à data de início"));
    }

    @Test
    @DisplayName("Retorna 409 quando o período se sobrepõe a outro recesso")
    void testeDadoSobreposicaoQuandoCriarEntaoRetornaConflito() throws Exception {
        when(service.create(new RecessoCreateDTO("Férias da Barbearia", INICIO, FIM)))
                .thenThrow(new RecessoConflitoException());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um recesso nesse período"));
    }

    @Test
    @DisplayName("Atualiza recesso existente e retorna 200")
    void testeDadoRecessoExistenteQuandoAtualizarEntaoRetornaOk() throws Exception {
        when(service.update(1L, new RecessoUpdateDTO("Reforma", null, null))).thenReturn(recesso(1L));

        mockMvc.perform(
                patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"Reforma\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Atualiza recesso inexistente e retorna 404")
    void testeDadoRecessoInexistenteQuandoAtualizarEntaoRetornaNaoEncontrado() throws Exception {
        when(service.update(99L, new RecessoUpdateDTO("Reforma", null, null)))
                .thenThrow(new RecessoNaoEncontradoException());

        mockMvc.perform(
                patch(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"Reforma\"}")
        ).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Recesso não encontrado"));
    }

    @Test
    @DisplayName("Remove recesso existente e retorna 204")
    void testeDadoRecessoExistenteQuandoDeletarEntaoRetornaSemConteudo() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("Remove recesso inexistente e retorna 404")
    void testeDadoRecessoInexistenteQuandoDeletarEntaoRetornaNaoEncontrado() throws Exception {
        doThrow(new RecessoNaoEncontradoException()).when(service).deleteById(99L);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista todos os recessos e retorna 200")
    void testeDadoRecessosCadastradosQuandoListarEntaoRetornaOk() throws Exception {
        when(service.findAll()).thenReturn(List.of(recesso(1L), recesso(2L)));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("Busca recesso por id e retorna 200")
    void testeDadoRecessoExistenteQuandoBuscarPorIdEntaoRetornaOk() throws Exception {
        when(service.findById(1L)).thenReturn(recesso(1L));

        mockMvc.perform(get(ENDPOINT + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Busca recesso inexistente e retorna 404")
    void testeDadoRecessoInexistenteQuandoBuscarPorIdEntaoRetornaNaoEncontrado() throws Exception {
        when(service.findById(99L)).thenThrow(new RecessoNaoEncontradoException());

        mockMvc.perform(get(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Restringe criar, atualizar e remover a DONO e ADMIN; leituras ficam abertas a qualquer autenticado")
    void testeDadoControllerQuandoLerAnotacoesEntaoEscritaExigeGestorELeituraNao() throws Exception {
        List<Method> escritas = List.of(
                RecessoController.class.getMethod("create", RecessoCreateDTO.class),
                RecessoController.class.getMethod("update", Long.class, RecessoUpdateDTO.class),
                RecessoController.class.getMethod("deleteById", Long.class)
        );
        List<Method> leituras = List.of(
                RecessoController.class.getMethod("findAll"),
                RecessoController.class.getMethod("findById", Long.class)
        );

        assertAll(
                escritas.stream()
                        .map(
                                metodo -> (Executable) () -> assertEquals(
                                        REGRA_GESTOR,
                                        metodo.getAnnotation(PreAuthorize.class).value()
                                )
                        )
        );
        assertAll(
                leituras.stream()
                        .map(metodo -> (Executable) () -> assertNull(metodo.getAnnotation(PreAuthorize.class)))
        );
    }

    private static RecessoEntity recesso(Long id) {
        return RecessoEntity.builder().id(id).descricao("Férias da Barbearia").dataInicio(INICIO).dataFim(FIM).build();
    }
}
