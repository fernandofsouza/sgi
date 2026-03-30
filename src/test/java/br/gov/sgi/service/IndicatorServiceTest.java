package br.gov.sgi.service;

import br.gov.sgi.dto.*;
import br.gov.sgi.entity.*;
import br.gov.sgi.repository.IndicatorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IndicatorService")
class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepo;

    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private IndicatorService service;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Indicator buildIndicator(String id, String title) {
        Indicator ind = new Indicator();
        ind.setId(id);
        ind.setSeqId(1);
        ind.setTitle(title);
        ind.setDescription("Descrição de teste");
        ind.setPdgId("PDG-001");
        ind.setCreationStatus("Aprovado");
        ind.setProgressStatus("Em andamento normal");
        ind.setProgress(50);
        ind.setReferenceYear(2026);
        ind.setReferenceRange("semestral");
        ind.setReferenceLabel("1S");
        ind.setTargetDate(LocalDate.of(2026, 6, 30));
        ind.setAssignees(new ArrayList<>());
        ind.setCheckIns(new ArrayList<>());
        ind.setCriteria(new ArrayList<>());
        ind.setAchievementScale(new ArrayList<>());
        ind.setRelevanceAssessments(new ArrayList<>());
        ind.setChildren(new ArrayList<>());
        ind.setCreatedAt(LocalDateTime.now());
        ind.setUpdatedAt(LocalDateTime.now());
        return ind;
    }

    private TeamMember buildMember(String id, String name) {
        TeamMember m = new TeamMember();
        m.setId(id);
        m.setName(name);
        m.setRole("Developer");
        m.setEmail(name.toLowerCase() + "@sgi.gov.br");
        m.setActive(true);
        return m;
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("deve retornar página de indicadores com filtros aplicados")
        void deveRetornarPaginaDeIndicadores() {
            Indicator ind = buildIndicator("1", "Indicador Teste");
            Page<Indicator> page = new PageImpl<>(List.of(ind));
            Pageable pageable = PageRequest.of(0, 20);

            when(indicatorRepo.findWithFilters(2026, "semestral", "1S", "Aprovado", null, pageable))
                    .thenReturn(page);

            Page<IndicatorSummaryDTO> result = service.findAll(2026, "semestral", "1S", "Aprovado", null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Indicador Teste");
            assertThat(result.getContent().get(0).getId()).isEqualTo("1");
            verify(indicatorRepo).findWithFilters(2026, "semestral", "1S", "Aprovado", null, pageable);
        }

        @Test
        @DisplayName("deve retornar página vazia quando não há indicadores")
        void deveRetornarPaginaVazia() {
            Page<Indicator> emptyPage = Page.empty();
            when(indicatorRepo.findWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(emptyPage);

            Page<IndicatorSummaryDTO> result = service.findAll(null, null, null, null, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar detalhe do indicador quando encontrado")
        void deveRetornarDetalhe() {
            Indicator ind = buildIndicator("abc-123", "Indicador Detalhado");
            when(indicatorRepo.findById("abc-123")).thenReturn(Optional.of(ind));

            IndicatorDetailDTO result = service.findById("abc-123");

            assertThat(result.getId()).isEqualTo("abc-123");
            assertThat(result.getTitle()).isEqualTo("Indicador Detalhado");
            assertThat(result.getCreationStatus()).isEqualTo("Aprovado");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(indicatorRepo.findById("id-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("id-inexistente"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("id-inexistente");
        }
    }

    // ── findRoots / findChildren ──────────────────────────────────────────────

    @Nested
    @DisplayName("findRoots e findChildren")
    class HierarquiaTests {

        @Test
        @DisplayName("deve retornar apenas indicadores sem pai")
        void deveRetornarIndicadoresRaiz() {
            Indicator root1 = buildIndicator("r1", "Raiz 1");
            Indicator root2 = buildIndicator("r2", "Raiz 2");
            when(indicatorRepo.findByParentIsNull()).thenReturn(List.of(root1, root2));

            List<IndicatorSummaryDTO> roots = service.findRoots();

            assertThat(roots).hasSize(2);
            assertThat(roots).extracting(IndicatorSummaryDTO::getId).containsExactly("r1", "r2");
        }

        @Test
        @DisplayName("deve retornar filhos de um indicador pai")
        void deveRetornarFilhos() {
            Indicator filho1 = buildIndicator("f1", "Filho 1");
            Indicator filho2 = buildIndicator("f2", "Filho 2");
            when(indicatorRepo.findByParentId("pai-1")).thenReturn(List.of(filho1, filho2));

            List<IndicatorSummaryDTO> filhos = service.findChildren("pai-1");

            assertThat(filhos).hasSize(2);
            assertThat(filhos).extracting(IndicatorSummaryDTO::getTitle)
                    .containsExactlyInAnyOrder("Filho 1", "Filho 2");
        }
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar indicador com status padrão quando não informado")
        void deveCriarComStatusPadrao() {
            CreateIndicatorDTO dto = new CreateIndicatorDTO();
            dto.setTitle("Novo Indicador");
            dto.setReferenceYear(2026);
            dto.setReferenceRange("anual");
            dto.setReferenceLabel("Anual");

            Indicator saved = buildIndicator("novo-id", "Novo Indicador");
            when(indicatorRepo.save(any(Indicator.class))).thenReturn(saved);

            IndicatorDetailDTO result = service.create(dto);

            assertThat(result.getTitle()).isEqualTo("Novo Indicador");
            verify(indicatorRepo).save(argThat(ind ->
                    ind.getCreationStatus().equals("Não iniciado") &&
                    ind.getProgressStatus().equals("Não iniciado") &&
                    ind.getProgress() == 0
            ));
        }

        @Test
        @DisplayName("deve criar indicador com escala de conquista padrão quando não fornecida")
        void deveCriarComEscalaPadrao() {
            CreateIndicatorDTO dto = new CreateIndicatorDTO();
            dto.setTitle("Indicador com escala padrão");
            dto.setReferenceYear(2026);
            dto.setReferenceRange("trimestral");
            dto.setReferenceLabel("1T");

            Indicator saved = buildIndicator("id-escala", "Indicador com escala padrão");
            when(indicatorRepo.save(any(Indicator.class))).thenReturn(saved);

            service.create(dto);

            verify(indicatorRepo).save(argThat(ind ->
                    ind.getAchievementScale().size() == 5
            ));
        }

        @Test
        @DisplayName("deve vincular indicador ao pai quando parentId informado")
        void deveVincularAoPai() {
            Indicator pai = buildIndicator("pai-id", "Indicador Pai");
            when(indicatorRepo.findById("pai-id")).thenReturn(Optional.of(pai));

            CreateIndicatorDTO dto = new CreateIndicatorDTO();
            dto.setTitle("Filho");
            dto.setParentId("pai-id");
            dto.setReferenceYear(2026);
            dto.setReferenceRange("anual");
            dto.setReferenceLabel("Anual");

            Indicator saved = buildIndicator("filho-id", "Filho");
            when(indicatorRepo.save(any())).thenReturn(saved);

            service.create(dto);

            verify(indicatorRepo).save(argThat(ind -> ind.getParent() != null && ind.getParent().getId().equals("pai-id")));
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("deve atualizar apenas campos não nulos")
        void deveAtualizarApenasNaoNulos() {
            Indicator existing = buildIndicator("upd-id", "Titulo Original");
            when(indicatorRepo.findById("upd-id")).thenReturn(Optional.of(existing));
            when(indicatorRepo.save(any())).thenReturn(existing);

            UpdateIndicatorDTO dto = new UpdateIndicatorDTO();
            dto.setTitle("Titulo Atualizado");
            dto.setProgress(75);
            // progressStatus e outros ficam null — não devem ser sobrescritos

            service.update("upd-id", dto);

            verify(indicatorRepo).save(argThat(ind ->
                    ind.getTitle().equals("Titulo Atualizado") &&
                    ind.getProgress() == 75 &&
                    ind.getProgressStatus().equals("Em andamento normal") // mantido
            ));
        }

        @Test
        @DisplayName("deve lançar exceção ao atualizar indicador inexistente")
        void deveLancarExcecaoQuandoInexistente() {
            when(indicatorRepo.findById("nao-existe")).thenReturn(Optional.empty());

            UpdateIndicatorDTO dto = new UpdateIndicatorDTO();
            dto.setTitle("Qualquer");

            assertThatThrownBy(() -> service.update("nao-existe", dto))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar indicador e desvinculá-lo dos filhos")
        void deveDeletarEDesvinculaFilhos() {
            Indicator filho = buildIndicator("f1", "Filho");
            Indicator pai   = buildIndicator("p1", "Pai");
            filho.setParent(pai);
            pai.setChildren(new ArrayList<>(List.of(filho)));

            when(indicatorRepo.findById("p1")).thenReturn(Optional.of(pai));

            service.delete("p1");

            assertThat(filho.getParent()).isNull();
            verify(indicatorRepo).delete(pai);
        }

        @Test
        @DisplayName("deve lançar exceção ao deletar ID inexistente")
        void deveLancarExcecaoQuandoInexistente() {
            when(indicatorRepo.findById("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("x"))
                    .isInstanceOf(EntityNotFoundException.class);
            verify(indicatorRepo, never()).delete(any());
        }
    }

    // ── addCheckIn ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addCheckIn")
    class AddCheckIn {

        @Test
        @DisplayName("deve registrar check-in e atualizar progresso do indicador")
        void deveRegistrarCheckInEAtualizarProgresso() {
            Indicator ind = buildIndicator("ind-1", "Indicador");
            TeamMember autor = buildMember("m1", "João");

            when(indicatorRepo.findById("ind-1")).thenReturn(Optional.of(ind));
            when(teamMemberService.findEntityById("m1")).thenReturn(autor);
            when(indicatorRepo.save(any())).thenReturn(ind);

            CreateCheckInDTO dto = new CreateCheckInDTO();
            dto.setCheckDate(LocalDate.now());
            dto.setProgress(80);
            dto.setNotes("Boa evolução");
            dto.setAuthorId("m1");

            CheckInDTO result = service.addCheckIn("ind-1", dto);

            assertThat(result.getProgress()).isEqualTo(80);
            assertThat(result.getNotes()).isEqualTo("Boa evolução");
            assertThat(ind.getProgress()).isEqualTo(80);
            assertThat(ind.getCheckIns()).hasSize(1);
        }

        @Test
        @DisplayName("deve atualizar valor atual do critério ao registrar check-in")
        void deveAtualizarCriterioNoCheckIn() {
            Indicator ind = buildIndicator("ind-2", "Com Critério");

            EvaluationCriteria criterio = new EvaluationCriteria();
            criterio.setId("c1");
            criterio.setCurrentValue(BigDecimal.valueOf(10));
            ind.setCriteria(new ArrayList<>(List.of(criterio)));

            TeamMember autor = buildMember("m1", "Ana");

            when(indicatorRepo.findById("ind-2")).thenReturn(Optional.of(ind));
            when(teamMemberService.findEntityById("m1")).thenReturn(autor);
            when(indicatorRepo.save(any())).thenReturn(ind);

            CreateCheckInDTO dto = new CreateCheckInDTO();
            dto.setCheckDate(LocalDate.now());
            dto.setProgress(60);
            dto.setAuthorId("m1");
            dto.setCriteriaUpdates(List.of(new CriteriaUpdateDTO("c1", BigDecimal.valueOf(35))));

            service.addCheckIn("ind-2", dto);

            assertThat(criterio.getCurrentValue()).isEqualByComparingTo(BigDecimal.valueOf(35));
        }
    }

    // ── updateRelevanceAssessments ────────────────────────────────────────────

    @Nested
    @DisplayName("updateRelevanceAssessments")
    class UpdateRelevance {

        @Test
        @DisplayName("deve substituir todas as avaliações de relevância existentes")
        void deveSubstituirAvaliacoes() {
            Indicator ind = buildIndicator("ind-r", "Com Relevância");
            RelevanceAssessment avaliacaoAntiga = new RelevanceAssessment();
            ind.setRelevanceAssessments(new ArrayList<>(List.of(avaliacaoAntiga)));

            when(indicatorRepo.findById("ind-r")).thenReturn(Optional.of(ind));
            when(indicatorRepo.save(any())).thenReturn(ind);

            List<RelevanceAssessmentDTO> novas = List.of(
                    new RelevanceAssessmentDTO("rc1", 5),
                    new RelevanceAssessmentDTO("rc2", 3)
            );

            service.updateRelevanceAssessments("ind-r", novas);

            assertThat(ind.getRelevanceAssessments()).hasSize(2);
            assertThat(ind.getRelevanceAssessments())
                    .extracting(RelevanceAssessment::getScore)
                    .containsExactlyInAnyOrder(5, 3);
        }
    }
}
