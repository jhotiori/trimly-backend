package org.trimly.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.service.auth.TokenService;
import org.trimly.backend.model.service.usuario.UsuarioService;

/**
 * Popula o `SecurityContext` a partir do token `Bearer`; token ausente ou inválido segue anônimo.
 *
 * Instanciado por `SecurityConfig`, sem `@Component`.
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    /**
     * Cabeçalho HTTP que carrega o token.
     */
    private static final String HEADER = "Authorization";

    /**
     * Prefixo do valor do cabeçalho, removido para obter o token.
     */
    private static final String PREFIX = "Bearer ";

    /**
     * Geração e validação dos tokens JWT.
     * @see {@link TokenService}
     */
    private final TokenService tokenService;

    /**
     * Regras de negócio de usuários.
     * @see {@link UsuarioService}
     */
    private final UsuarioService usuarioService;

    /**
     * Resolve o usuário a partir do token `Bearer` e o coloca no `SecurityContext`. Um
     * token ausente ou inválido não interrompe a requisição: o contexto é limpo e a cadeia segue
     * anônima.
     *
     * @param request - requisição HTTP recebida
     * @param response - resposta HTTP associada
     * @param filterChain - cadeia de filtros a continuar
     * @throws ServletException - quando um filtro seguinte da cadeia falha
     * @throws IOException - quando a leitura da requisição ou a escrita da resposta falha
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getRequestToken(request);
        if (token != null) {
            try {
                String email = tokenService.validateToken(token);
                UsuarioEntity usuario = usuarioService.findByEmail(email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception exception) {
                log.debug("token rejeitado: {}", exception.toString());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token do cabeçalho `Authorization`, removendo o prefixo `Bearer`.
     *
     * @param request - requisição HTTP recebida
     * @return String - o token sem o prefixo, ou nulo quando o cabeçalho está ausente ou malformado
     */
    private String getRequestToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            return null;
        }
        return header.substring(PREFIX.length()).trim();
    }
}
