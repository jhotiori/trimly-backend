package org.trimly.backend.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.trimly.backend.config.FlywayConfig;

/**
 * Combina {@code @DataJpaTest} com a importação de {@link FlywayConfig}, de forma que o schema de teste seja
 * criado pelo mesmo caminho de migração usado em produção e desenvolvimento, em vez de gerado automaticamente
 * pelo Hibernate.
 *
 * Toda classe de teste de repositório deve usar esta anotação em vez de compor {@code @DataJpaTest}
 * manualmente. Como {@link FlywayConfig#migrate()} só executa uma vez por JVM, uma classe com uma configuração
 * de contexto diferente receberia um contexto novo do Spring, com um banco H2 ainda não migrado, e falharia a
 * validação de schema do Hibernate.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@Import(FlywayConfig.class)
public @interface RepositoryTest {}
