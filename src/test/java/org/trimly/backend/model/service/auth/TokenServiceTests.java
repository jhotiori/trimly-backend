package org.trimly.backend.model.service.auth;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Testes para {@link TokenService}.
 */
class TokenServiceTests {
    /**
     * Serviço sob teste, com segredo e expiração fixos.
     * @see {@link TokenService}
     */
    private final TokenService service = new TokenService("segredo-de-teste", 3600);

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
        String token = new TokenService("outro-segredo", 3600).generateToken(usuario());

        assertThrows(JWTVerificationException.class, () -> service.validateToken(token));
    }

    @Test
    @DisplayName("Rejeita token expirado")
    void testeDadoTokenExpiradoQuandoValidarEntaoLancaTokenExpirado() {
        String token = new TokenService("segredo-de-teste", -60).generateToken(usuario());

        assertThrows(TokenExpiredException.class, () -> service.validateToken(token));
    }

    @Test
    @DisplayName("Rejeita token malformado")
    void testeDadoTokenMalformadoQuandoValidarEntaoLancaExcecao() {
        assertThrows(JWTVerificationException.class, () -> service.validateToken("nao-e-um-jwt"));
    }

    private static UsuarioEntity usuario() {
        return UsuarioEntity.builder().id(1L).email("ana@trimly.com").cargo(UsuarioCargo.ADMIN).build();
    }
}
