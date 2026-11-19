package org.trimly.backend.infra.security.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades dos tokens JWT lidas de `trimly.security.jwt`.
 *
 * O segredo fica fora do record: o binder aceitaria `${JWT_SECRET}` sem resolver, e o `@Value` de {@link JwtConfig}
 * falha a inicialização quando a variável falta.
 *
 * @param issuer - emissor gravado e exigido no token
 * @param expiry - tempo de validade do token, em segundos
 */
@ConfigurationProperties("trimly.security.jwt")
public record JwtProperties(String issuer, long expiry) {}
