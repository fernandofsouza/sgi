package br.gov.sgi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configura o DataSource no Heroku a partir da variável DATABASE_URL,
 * que o add-on Heroku Postgres injeta no formato:
 *   postgres://usuario:senha@host:porta/banco
 *
 * O Spring Boot não lê esse formato diretamente, então fazemos o parse aqui.
 * Isso garante que rotações automáticas de credenciais pelo Heroku sejam
 * absorvidas sem necessidade de atualizar Config Vars manualmente.
 */
@Configuration
@Profile("heroku")
public class HerokuDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws Exception {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                "Config Var DATABASE_URL não encontrada. " +
                "Verifique se o add-on Heroku Postgres está provisionado."
            );
        }

        URI dbUri = new URI(databaseUrl);
        String[] userInfo = dbUri.getUserInfo().split(":", 2);
        String username = userInfo[0];
        String password = userInfo[1];
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost()
                + ':' + dbUri.getPort()
                + dbUri.getPath()
                + "?sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);

        return new HikariDataSource(config);
    }
}
