package org.trimly.backend.model.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

/**
 * Gera e valida os tokens JWT stateless usados na autenticação.
 *
 * O token carrega o e-mail do usuário no {@code subject} e o cargo em uma claim
 * {@code cargo}. A assinatura usa HMAC256 com o segredo configurado em
 * {@code trimly.security.jwt.secret} e expira após {@code trimly.security.jwt.expiration}
 * segundos.
 */
@Service
public class TokenService {
    private static final String ISSUER = "trimly-auth-api";
    private static final String CLAIM_CARGO = "cargo";

    private final String secret;
    private final Integer expiration;

    /**
     * Injeta o segredo e o tempo de expiração da configuração da aplicação.
     *
     * @param secret - segredo HMAC usado para assinar e verificar os tokens
     * @param expiration - tempo de validade do token, em segundos
     */
    public TokenService(
            @Value("${trimly.security.jwt.secret}") String secret,
            @Value("${trimly.security.jwt.expiration}") Integer expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * Gera um token assinado para o usuário informado.
     *
     * @param usuario - usuário autenticado dono do token
     * @return String - o token JWT assinado
     */
    public String generateToken(UsuarioEntity usuario) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuario.getEmail())
                .withClaim(CLAIM_CARGO, usuario.getCargo().toString())
                .withExpiresAt(generateExpirationTime())
                .sign(algorithm);
    }

    /**
     * Valida a assinatura e a expiração do token e retorna o e-mail contido no {@code subject}.
     *
     * @param token - token JWT recebido na requisição
     * @throws JWTVerificationException - quando o token é inválido, expirado ou não confere com a assinatura
     * @return String - o e-mail do usuário dono do token
     */
    public String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject();
    }

    /**
     * Gera o tempo de expiração de um token.
     *
     * @return Instant - o tempo de expiração calculado.
     */
    private Instant generateExpirationTime() {
        return Instant.now().plusSeconds(expiration);
    }
}
