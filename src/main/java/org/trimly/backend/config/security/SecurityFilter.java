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
 * Lê o token {@code Bearer} da requisição, resolve o usuário e popula o
 * {@code SecurityContext} antes da cadeia de filtros do Spring Security.
 *
 * Instanciado diretamente por {@code SecurityConfig}, sem {@code @Component}, para
 * não ser registrado também no chain de filtros do servlet.
 *
 * Enquanto a autorização não é aplicada, um token ausente ou inválido não
 * interrompe a requisição: o contexto fica anônimo e o request segue. O
 * tratamento de {@code 401} para token inválido entra junto com a implementação da
 * autorização.
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private static final String HEADER = "Authorization";
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
     * Resolve o usuário a partir do token {@code Bearer} e o coloca no {@code SecurityContext}. Um
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
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception exception) {
                log.debug("token rejeitado: {}", exception.toString());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token do cabeçalho {@code Authorization}, removendo o prefixo {@code Bearer}.
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
