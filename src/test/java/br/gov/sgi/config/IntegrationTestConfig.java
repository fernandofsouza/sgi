package br.gov.sgi.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuração compartilhada para testes de integração.
 *
 * Ao iniciar o contexto Spring, executa flyway.clean() seguido de
 * flyway.migrate(), garantindo um banco limpo com o schema atualizado
 * a cada execução. Requer clean-disabled: false no application-integration-test.yml.
 */
@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();    // apaga todas as tabelas do banco de teste
            flyway.migrate();  // recria o schema a partir das migrations V*__.sql
        };
    }
}
