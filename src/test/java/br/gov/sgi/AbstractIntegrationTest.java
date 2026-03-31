package br.gov.sgi;

import br.gov.sgi.config.IntegrationTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe base para testes de integração contra o PostgreSQL do Heroku.
 *
 * Ao subir, o contexto Spring executa clean() + migrate() via Flyway,
 * garantindo um schema limpo e atualizado.
 *
 * Uso:
 *   - Estenda esta classe em arquivos *IT.java
 *   - Anote métodos que alteram dados com @Transactional para rollback automático
 *
 * Variáveis de ambiente necessárias (localmente ou no Heroku CI):
 *   JDBC_DATABASE_URL, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
public abstract class AbstractIntegrationTest {
}
