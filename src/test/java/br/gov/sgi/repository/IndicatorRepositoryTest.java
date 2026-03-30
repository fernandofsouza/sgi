package br.gov.sgi.repository;

import br.gov.sgi.entity.Indicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("IndicatorRepository")
class IndicatorRepositoryTest {

    @Autowired
    private IndicatorRepository repo;

    private Indicator buildAndSave(String id, String title, int year, String range, String label,
                                    String creationStatus, String progressStatus) {
        Indicator ind = new Indicator();
        ind.setId(id);
        ind.setTitle(title);
        ind.setCreationStatus(creationStatus);
        ind.setProgressStatus(progressStatus);
        ind.setProgress(50);
        ind.setReferenceYear(year);
        ind.setReferenceRange(range);
        ind.setReferenceLabel(label);
        ind.setTargetDate(LocalDate.of(year, 12, 31));
        ind.setAssignees(new ArrayList<>());
        ind.setCheckIns(new ArrayList<>());
        ind.setCriteria(new ArrayList<>());
        ind.setAchievementScale(new ArrayList<>());
        ind.setRelevanceAssessments(new ArrayList<>());
        ind.setChildren(new ArrayList<>());
        ind.setCreatedAt(LocalDateTime.now());
        ind.setUpdatedAt(LocalDateTime.now());
        return repo.save(ind);
    }

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Nested
    @DisplayName("findWithFilters")
    class FindWithFilters {

        @Test
        @DisplayName("deve filtrar por ano de referência")
        void deveFiltrarPorAno() {
            buildAndSave("i1", "Ind 2026", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            buildAndSave("i2", "Ind 2025", 2025, "anual", "Anual", "Aprovado", "Em andamento normal");

            Page<Indicator> result = repo.findWithFilters(2026, null, null, null, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("i1");
        }

        @Test
        @DisplayName("deve filtrar por range e label de referência")
        void deveFiltrarPorRangeELabel() {
            buildAndSave("i1", "Semestral 1S", 2026, "semestral", "1S", "Aprovado", "Em andamento normal");
            buildAndSave("i2", "Semestral 2S", 2026, "semestral", "2S", "Aprovado", "Em andamento normal");
            buildAndSave("i3", "Trimestral",   2026, "trimestral","1T", "Aprovado", "Em andamento normal");

            Page<Indicator> result = repo.findWithFilters(2026, "semestral", "1S", null, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Semestral 1S");
        }

        @Test
        @DisplayName("deve filtrar por status de criação")
        void deveFiltrarPorCreationStatus() {
            buildAndSave("i1", "Aprovado",   2026, "anual", "Anual", "Aprovado",  "Em andamento normal");
            buildAndSave("i2", "Em edição",  2026, "anual", "Anual", "Em edição", "Não iniciado");

            Page<Indicator> result = repo.findWithFilters(null, null, null, "Aprovado", null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCreationStatus()).isEqualTo("Aprovado");
        }

        @Test
        @DisplayName("deve retornar todos quando filtros são nulos")
        void deveRetornarTodosSemFiltro() {
            buildAndSave("i1", "Ind A", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            buildAndSave("i2", "Ind B", 2025, "semestral", "1S", "Em edição", "Não iniciado");

            Page<Indicator> result = repo.findWithFilters(null, null, null, null, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByParentIsNull")
    class FindRoots {

        @Test
        @DisplayName("deve retornar apenas indicadores sem pai")
        void deveRetornarSemPai() {
            Indicator raiz1 = buildAndSave("r1", "Raiz 1", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            Indicator raiz2 = buildAndSave("r2", "Raiz 2", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");

            Indicator filho = buildAndSave("f1", "Filho", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            filho.setParent(raiz1);
            repo.save(filho);

            List<Indicator> raizes = repo.findByParentIsNull();

            assertThat(raizes).hasSize(2);
            assertThat(raizes).extracting(Indicator::getId).containsExactlyInAnyOrder("r1", "r2");
        }
    }

    @Nested
    @DisplayName("findByParentId")
    class FindChildren {

        @Test
        @DisplayName("deve retornar filhos de um indicador pai")
        void deveRetornarFilhos() {
            Indicator pai = buildAndSave("pai", "Pai", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            Indicator f1  = buildAndSave("f1", "Filho 1", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");
            Indicator f2  = buildAndSave("f2", "Filho 2", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");

            f1.setParent(pai);
            f2.setParent(pai);
            repo.save(f1);
            repo.save(f2);

            List<Indicator> filhos = repo.findByParentId("pai");

            assertThat(filhos).hasSize(2);
            assertThat(filhos).extracting(Indicator::getId).containsExactlyInAnyOrder("f1", "f2");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando pai não tem filhos")
        void deveRetornarVazioSemFilhos() {
            buildAndSave("sozinho", "Sem filhos", 2026, "anual", "Anual", "Aprovado", "Em andamento normal");

            assertThat(repo.findByParentId("sozinho")).isEmpty();
        }
    }
}
