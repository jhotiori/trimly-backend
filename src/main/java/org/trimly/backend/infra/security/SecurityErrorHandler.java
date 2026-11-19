package org.trimly.backend.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import tools.jackson.databind.json.JsonMapper;

/**
 * Escreve `401` e `403` da cadeia de filtros no corpo padrão `ErrorResponseDTO`.
 *
 * Os filtros rodam antes do dispatcher, então a resposta é serializada aqui e não por `@RestControllerAdvice`.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    /**
     * Mensagem fixa do `401`.
     */
    public static final String NAO_AUTENTICADO = "Autenticação necessária";

    /**
     * Mensagem fixa do `403`.
     */
    public static final String ACESSO_NEGADO = "Acesso negado";

    /**
     * Serializador JSON do corpo de erro.
     * @see {@link JsonMapper}
     */
    private final JsonMapper jsonMapper;

    /**
     * Responde `401` para requisição sem token ou com token inválido em rota protegida.
     *
     * @param request - requisição HTTP recebida
     * @param response - resposta HTTP associada
     * @param exception - falha de autenticação
     * @throws IOException - quando a escrita da resposta falha
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        log.warn("nao autenticado: rota={} {}", request.getMethod(), request.getRequestURI());
        write(response, HttpStatus.UNAUTHORIZED, NAO_AUTENTICADO);
    }

    /**
     * Responde `403` para usuário autenticado sem permissão na cadeia de filtros.
     *
     * @param request - requisição HTTP recebida
     * @param response - resposta HTTP associada
     * @param exception - falha de autorização
     * @throws IOException - quando a escrita da resposta falha
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        logAcessoNegado(request);
        write(response, HttpStatus.FORBIDDEN, ACESSO_NEGADO);
    }

    /**
     * Registra a negação de acesso com o usuário e a rota requisitada.
     *
     * @param request - requisição HTTP negada
     */
    public static void logAcessoNegado(HttpServletRequest request) {
        String usuario = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
        log.warn("acesso negado: usuario={}, rota={} {}", usuario, request.getMethod(), request.getRequestURI());
    }

    /**
     * Escreve o corpo de erro em JSON com o status informado.
     *
     * @param response - resposta HTTP associada
     * @param status - status HTTP da resposta
     * @param message - mensagem fixa do erro
     * @throws IOException - quando a escrita da resposta falha
     */
    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getOutputStream(), ErrorResponseDTO.of(status, message));
    }
}
