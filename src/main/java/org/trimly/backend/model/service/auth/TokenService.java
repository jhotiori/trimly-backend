package org.trimly.backend.model.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trimly.backend.infra.security.authentication.JwtProperties;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

/**
 * Gera e valida tokens JWT stateless (HMAC256), com e-mail no `subject` e cargo na claim `cargo`.
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    /**
     * Nome da claim que carrega o cargo do usuário.
     */
    private static final String CLAIM_CARGO = "cargo";

    /**
     * Algoritmo usado para assinar os tokens.
     * @see {@link Algorithm}
     */
    private final Algorithm algorithm;

    /**
     * Verificador de assinatura, emissor e expiração dos tokens.
     * @see {@link JWTVerifier}
     */
    private final JWTVerifier verifier;

    /**
     * Propriedades dos tokens JWT (emissor e expiração).
     * @see {@link JwtProperties}
     */
    private final JwtProperties properties;

    /**
     * Gera um token assinado para o usuário informado.
     *
     * @param usuario - usuário autenticado dono do token
     * @return String - o token JWT assinado
     */
    public String generateToken(UsuarioEntity usuario) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(properties.issuer())
                .withIssuedAt(now)
                .withSubject(usuario.getEmail())
                .withClaim(CLAIM_CARGO, usuario.getCargo().toString())
                .withExpiresAt(now.plusSeconds(properties.expiry()))
                .sign(algorithm);
    }

    /**
     * Valida a assinatura, o emissor e a expiração do token e retorna o e-mail contido no `subject`.
     *
     * @param token - token JWT recebido na requisição
     * @throws JWTVerificationException - quando o token é inválido, expirado ou não confere com a assinatura
     * @return String - o e-mail do usuário dono do token
     */
    public String validateToken(String token) {
        return verifier.verify(token).getSubject();
    }
}
