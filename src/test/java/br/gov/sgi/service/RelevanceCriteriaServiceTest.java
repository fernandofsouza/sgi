package br.gov.sgi.service;

import br.gov.sgi.dto.RelevanceCriteriaDTO;
import br.gov.sgi.dto.ScaleLabelDTO;
import br.gov.sgi.entity.RelevanceCriteria;
import br.gov.sgi.repository.RelevanceCriteriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelevanceCriteriaService")
class RelevanceCriteriaServiceTest {

    @Mock
    private RelevanceCriteriaRepository repo;

    @InjectMocks
    private RelevanceCriteriaService service;

    private RelevanceCriteria buildCriteria(String id, String name) {
        RelevanceCriteria c = new RelevanceCriteria();
        c.setId(id);
        c.setName(name);
        c.setDescription("Descrição do critério " + name);
        c.setActive(true);
        c.setScaleLabels(new ArrayList<>());
        return c;
    }

    private List<ScaleLabelDTO> defaultScaleLabels() {
        return List.of(
                new ScaleLabelDTO(1, "Baixo"),
                new ScaleLabelDTO(2, "Médio"),
                new ScaleLabelDTO(3, "Alto"),
                new ScaleLabelDTO(4, "Muito Alto"),
                new ScaleLabelDTO(5, "Máximo")
        );
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll retorna apenas critérios ativos")
    void findAllDeveRetornarApenasAtivos() {
        RelevanceCriteria c1 = buildCriteria("rc1", "Impacto Estratégico");
        RelevanceCriteria c2 = buildCriteria("rc2", "Viabilidade");
        when(repo.findByActiveTrue()).thenReturn(List.of(c1, c2));

        List<RelevanceCriteriaDTO> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RelevanceCriteriaDTO::getName)
                .containsExactlyInAnyOrder("Impacto Estratégico", "Viabilidade");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById retorna critério existente")
    void findByIdDeveRetornarExistente() {
        when(repo.findById("rc1")).thenReturn(Optional.of(buildCriteria("rc1", "Impacto")));

        RelevanceCriteriaDTO result = service.findById("rc1");

        assertThat(result.getId()).isEqualTo("rc1");
        assertThat(result.getName()).isEqualTo("Impacto");
    }

    @Test
    @DisplayName("findById lança exceção para critério inexistente")
    void findByIdDeveLancarExcecaoParaInexistente() {
        when(repo.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("x"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("x");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create persiste critério com labels de escala")
    void createDevePersisteComLabels() {
        RelevanceCriteriaDTO dto = RelevanceCriteriaDTO.builder()
                .name("Novo Critério")
                .description("Descrição")
                .scaleLabels(defaultScaleLabels())
                .build();

        when(repo.save(any())).thenAnswer(inv -> {
            RelevanceCriteria saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID().toString());
            return saved;
        });

        RelevanceCriteriaDTO result = service.create(dto);

        assertThat(result.getName()).isEqualTo("Novo Critério");
        assertThat(result.getActive()).isTrue();
        verify(repo).save(argThat(c ->
                c.getName().equals("Novo Critério") &&
                c.getActive() == Boolean.TRUE &&
                c.getScaleLabels().size() == 5
        ));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update atualiza nome e descrição parcialmente")
    void updateDeveAtualizarParcialmente() {
        RelevanceCriteria existing = buildCriteria("rc1", "Nome Antigo");

        RelevanceCriteriaDTO dto = RelevanceCriteriaDTO.builder()
                .name("Nome Novo")
                .build();

        when(repo.findById("rc1")).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RelevanceCriteriaDTO result = service.update("rc1", dto);

        assertThat(result.getName()).isEqualTo("Nome Novo");
        assertThat(result.getDescription()).isEqualTo("Descrição do critério Nome Antigo"); // não alterado
    }

    // ── delete (soft delete) ──────────────────────────────────────────────────

    @Test
    @DisplayName("delete marca critério como inativo")
    void deleteDeveFazerSoftDelete() {
        RelevanceCriteria criteria = buildCriteria("rc1", "Impacto");

        when(repo.findById("rc1")).thenReturn(Optional.of(criteria));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.delete("rc1");

        assertThat(criteria.getActive()).isFalse();
        verify(repo).save(criteria);
        verify(repo, never()).delete(any());
    }
}
