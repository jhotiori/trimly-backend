package org.trimly.backend.model.service.auth;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trimly.backend.infra.security.authentication.JwtConfig;
import org.trimly.backend.infra.security.authentication.JwtProperties;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Testes para {@link TokenService}.
 */
class TokenServiceTests {
    /**
     * Segredo HMAC do serviço sob teste.
     */
    private static final String SEGREDO = "segredo-de-teste";

    /**
     * Serviço sob teste, com segredo e expiração fixos.
     * @see {@link TokenService}
     */
    private final TokenService service = tokenService(SEGREDO, 3600);

    @Test
    @DisplayName("Gera token com emissor, e-mail, cargo e expiração futura")
    void testeDadoUsuarioQuandoGerarTokenEntaoContemClaimsEsperadas() {
        String token = service.generateToken(usuario());

        DecodedJWT decodificado = JWT.decode(token);

        assertAll(
                () -> assertEquals("trimly-auth-api", decodificado.getIssuer()),
                () -> assertEquals("ana@trimly.com", decodificado.getSubject()),
                () -> assertEquals("ADMIN", decodificado.getClaim("cargo").asString()),
                () -> assertTrue(decodificado.getExpiresAtAsInstant().isAfter(Instant.now()))
        );
    }

    @Test
    @DisplayName("Valida token gerado e devolve o e-mail")
    void testeDadoTokenValidoQuandoValidarEntaoRetornaEmail() {
        String token = service.generateToken(usuario());

        String email = service.validateToken(token);

        assertEquals("ana@trimly.com", email);
    }

    @Test
    @DisplayName("Rejeita token assinado com outro segredo")
    void testeDadoTokenDeOutroSegredoQuandoValidarEntaoLancaExcecao() {
        String token = tokenService("outro-segredo", 3600).generateToken(usuario());

        assertThrows(JWTVerificationException.class, () -> service.validateToken(token));
    }

    @Test
    @DisplayName("Rejeita token expirado")
    void testeDadoTokenExpiradoQuandoValidarEntaoLancaTokenExpirado() {
        String token = tokenService(SEGREDO, -60).generateToken(usuario());

        assertThrows(TokenExpiredException.class, () -> service.validateToken(token));
    }

    @Test
    @DisplayName("Rejeita token malformado")
    void testeDadoTokenMalformadoQuandoValidarEntaoLancaExcecao() {
        assertThrows(JWTVerificationException.class, () -> service.validateToken("nao-e-um-jwt"));
    }

    private static TokenService tokenService(String segredo, long expiracao) {
        JwtConfig config = new JwtConfig();
        Algorithm algorithm = config.jwtAlgorithm(segredo);
        JwtProperties properties = new JwtProperties("trimly-auth-api", expiracao);
        return new TokenService(algorithm, config.jwtVerifier(algorithm, properties), properties);
    }

    private static UsuarioEntity usuario() {
        return UsuarioEntity.builder().id(1L).email("ana@trimly.com").cargo(UsuarioCargo.ADMIN).build();
    }
}
