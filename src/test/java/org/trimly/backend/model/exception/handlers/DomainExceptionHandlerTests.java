package org.trimly.backend.model.exception.handlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.TrimlyException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoNoPassadoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;

/**
 * Testes para {@link DomainExceptionHandler}.
 */
@WebMvcTest(DomainExceptionHandlerTests.ErroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DomainExceptionHandlerTests.ErroController.class)
class DomainExceptionHandlerTests {
    /**
     * Cliente HTTP simulado para exercitar o handler.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Converte entidade não encontrada em 404")
    void testeDadoEntidadeNaoEncontradaQuandoLancadaEntaoRetornaNaoEncontrado() throws Exception {
        mockMvc.perform(get("/erro/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Não encontrado"));
    }

    @Test
    @DisplayName("Converte conflito de agendamento em 409")
    void testeDadoConflitoDeAgendamentoQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/agendamento-conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Horário ocupado"));
    }

    @Test
    @DisplayName("Converte agendamento em feriado em 409")
    void testeDadoAgendamentoEmFeriadoQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/agendamento-feriado")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte conflito de disponibilidade em 409")
    void testeDadoConflitoDeDisponibilidadeQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/disponibilidade-conflito")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte nome de serviço duplicado em 409")
    void testeDadoNomeDeServicoDuplicadoQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/servico-duplicado")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte e-mail já cadastrado em 409")
    void testeDadoEmailJaCadastradoQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/usuario-email")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte serviço com agendamento pendente em 409")
    void testeDadoServicoComAgendamentoPendenteQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/servico-pendente")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte usuário com agendamento pendente em 409")
    void testeDadoUsuarioComAgendamentoPendenteQuandoLancadaEntaoRetornaConflito() throws Exception {
        mockMvc.perform(get("/erro/usuario-pendente")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Converte regra de domínio sem tratamento próprio em 422")
    void testeDadoRegraDeDominioGenericaQuandoLancadaEntaoRetornaUnprocessable() throws Exception {
        mockMvc.perform(get("/erro/servico-regra"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Regra violada"));
    }

    @Test
    @DisplayName("Converte agendamento no passado em 422")
    void testeDadoAgendamentoNoPassadoQuandoLancadaEntaoRetornaUnprocessable() throws Exception {
        mockMvc.perform(get("/erro/agendamento-passado")).andExpect(status().isUnprocessableContent());
    }

    @RestController
    static class ErroController {
        private static final Map<String, TrimlyException> EXCECOES = Map.ofEntries(
                Map.entry("nao-encontrado", new EntityNotFoundException("Não encontrado")),
                Map.entry("agendamento-conflito", new AgendamentoConflitoException("Horário ocupado")),
                Map.entry("agendamento-feriado", new AgendamentoFeriadoException("Feriado")),
                Map.entry("disponibilidade-conflito", new DisponibilidadeConflitoException("Janela ocupada")),
                Map.entry("servico-duplicado", new ServicoNomeDuplicadoException("Nome duplicado")),
                Map.entry("usuario-email", new UsuarioEmailExistenteException("E-mail em uso")),
                Map.entry("servico-pendente", new ServicoComAgendamentoPendenteException("Serviço pendente")),
                Map.entry("usuario-pendente", new UsuarioComAgendamentoPendenteException("Usuário pendente")),
                Map.entry("servico-regra", new ServicoException("Regra violada")),
                Map.entry("agendamento-passado", new AgendamentoNoPassadoException("No passado"))
        );

        @GetMapping("/erro/{tipo}")
        void lancar(@PathVariable String tipo) {
            throw EXCECOES.get(tipo);
        }
    }
}
