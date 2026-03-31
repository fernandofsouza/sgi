package br.gov.sgi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Migrations Flyway")
class FlywayMigrationIT extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private static final List<String> TABELAS_ESPERADAS = List.of(
        "team_members",
        "relevance_criteria",
        "relevance_scale_labels",
        "indicators",
        "achievement_scale_labels",
        "evaluation_criteria",
        "indicator_assignees",
        "check_ins",
        "check_in_criteria_updates",
        "relevance_assessments",
        "member_weights",
        "audit_logs",
        "system_configs"
    );

    @Test
    @DisplayName("deve criar todas as tabelas definidas na migration V1")
    void deveCriarTodasAsTabelas() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"});
            List<String> tabelasEncontradas = new ArrayList<>();
            while (rs.next()) {
                tabelasEncontradas.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            assertThat(tabelasEncontradas).containsAll(TABELAS_ESPERADAS);
        }
    }

    @Test
    @DisplayName("deve inserir os dados iniciais em system_configs")
    void deveInserirSystemConfigs() throws Exception {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT config_key FROM system_configs ORDER BY config_key");
             ResultSet rs = stmt.executeQuery()) {

            List<String> keys = new ArrayList<>();
            while (rs.next()) keys.add(rs.getString("config_key"));

            assertThat(keys).containsExactlyInAnyOrder("creation_statuses", "progress_statuses");
        }
    }

    @Test
    @DisplayName("deve inserir o critério de relevância inicial")
    void deveInserirRelevanceCriteria() throws Exception {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT id, name FROM relevance_criteria WHERE active = TRUE");
             ResultSet rs = stmt.executeQuery()) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("id")).isEqualTo("rc1");
            assertThat(rs.getString("name")).isEqualTo("Impacto Estratégico");
        }
    }

    @Test
    @DisplayName("deve inserir as 5 labels da escala de relevância")
    void deveInserirRelevanceScaleLabels() throws Exception {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(
                 "SELECT scale_value, label FROM relevance_scale_labels WHERE criterion_id = 'rc1' ORDER BY scale_value");
             ResultSet rs = stmt.executeQuery()) {

            List<String> labels = new ArrayList<>();
            while (rs.next()) labels.add(rs.getString("label"));

            assertThat(labels).containsExactly("Baixo", "Médio", "Alto", "Muito Alto", "Máximo");
        }
    }

    @Test
    @DisplayName("deve inserir os 5 membros iniciais da equipe")
    void deveInserirTeamMembers() throws Exception {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM team_members WHERE active = TRUE");
             ResultSet rs = stmt.executeQuery()) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("deve criar os índices de performance")
    void deveCriarIndices() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getIndexInfo(null, "public", "indicators", false, false);
            List<String> indices = new ArrayList<>();
            while (rs.next()) {
                String nome = rs.getString("INDEX_NAME");
                if (nome != null) indices.add(nome.toLowerCase());
            }
            assertThat(indices).contains(
                "ix_indicators_parent_id",
                "ix_indicators_reference_year"
            );
        }
    }
}
