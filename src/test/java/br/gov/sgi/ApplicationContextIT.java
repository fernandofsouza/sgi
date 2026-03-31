package br.gov.sgi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Contexto da aplicação")
class ApplicationContextIT extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("deve carregar o contexto Spring sem erros")
    void deveCarregarContexto() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    @DisplayName("deve conectar ao PostgreSQL com sucesso")
    void deveConectarAoBancoDeDados() {
        assertThatNoException().isThrownBy(() -> {
            try (Connection conn = dataSource.getConnection()) {
                assertThat(conn.isValid(2)).isTrue();
            }
        });
    }

    @Test
    @DisplayName("deve estar conectado ao PostgreSQL e não ao H2")
    void deveUsarPostgreSQL() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String driverName = conn.getMetaData().getDatabaseProductName();
            assertThat(driverName).containsIgnoringCase("PostgreSQL");
        }
    }
}
