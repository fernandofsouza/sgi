package br.gov.sgi.service;

import br.gov.sgi.dto.TeamMemberDTO;
import br.gov.sgi.entity.TeamMember;
import br.gov.sgi.repository.TeamMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamMemberService")
class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository repo;

    @InjectMocks
    private TeamMemberService service;

    private TeamMember buildMember(String id, String name, boolean active) {
        TeamMember m = new TeamMember();
        m.setId(id);
        m.setName(name);
        m.setRole("Developer");
        m.setEmail(name.toLowerCase().replace(" ", ".") + "@sgi.gov.br");
        m.setActive(active);
        return m;
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("deve retornar apenas membros ativos")
        void deveRetornarApenasAtivos() {
            when(repo.findByActiveTrue()).thenReturn(List.of(
                    buildMember("1", "Ana Silva", true),
                    buildMember("2", "Carlos Mendes", true)
            ));

            List<TeamMemberDTO> result = service.findAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(TeamMemberDTO::getName)
                    .containsExactly("Ana Silva", "Carlos Mendes");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há membros ativos")
        void deveRetornarListaVazia() {
            when(repo.findByActiveTrue()).thenReturn(List.of());

            assertThat(service.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findEntityById")
    class FindEntityById {

        @Test
        @DisplayName("deve retornar membro quando ID existe")
        void deveRetornarMembro() {
            TeamMember m = buildMember("m1", "Diego Rocha", true);
            when(repo.findById("m1")).thenReturn(Optional.of(m));

            TeamMember result = service.findEntityById("m1");

            assertThat(result.getId()).isEqualTo("m1");
            assertThat(result.getName()).isEqualTo("Diego Rocha");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando ID não existe")
        void deveLancarExcecao() {
            when(repo.findById("xx")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findEntityById("xx"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("xx");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar membro ativo com ID gerado automaticamente")
        void deveCriarMembroAtivo() {
            TeamMemberDTO dto = TeamMemberDTO.builder()
                    .name("Fernanda Costa")
                    .role("QA Lead")
                    .email("fernanda@sgi.gov.br")
                    .build();

            TeamMember saved = buildMember("gen-id", "Fernanda Costa", true);
            when(repo.save(any())).thenReturn(saved);

            TeamMemberDTO result = service.create(dto);

            assertThat(result.getName()).isEqualTo("Fernanda Costa");
            assertThat(result.getActive()).isTrue();
            verify(repo).save(argThat(m ->
                    m.getName().equals("Fernanda Costa") &&
                    m.getActive() &&
                    m.getId() != null
            ));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("deve atualizar apenas campos não nulos")
        void deveAtualizarApenasNaoNulos() {
            TeamMember existing = buildMember("u1", "Beatriz Lima", true);
            existing.setRole("Designer");
            when(repo.findById("u1")).thenReturn(Optional.of(existing));
            when(repo.save(any())).thenReturn(existing);

            TeamMemberDTO dto = TeamMemberDTO.builder()
                    .role("Senior Designer")
                    .build(); // name e email ficam null

            service.update("u1", dto);

            verify(repo).save(argThat(m ->
                    m.getRole().equals("Senior Designer") &&
                    m.getName().equals("Beatriz Lima") // mantido
            ));
        }

        @Test
        @DisplayName("deve lançar exceção ao atualizar membro inexistente")
        void deveLancarExcecao() {
            when(repo.findById("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update("x", new TeamMemberDTO()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve fazer soft delete marcando membro como inativo")
        void deveFazerSoftDelete() {
            TeamMember m = buildMember("d1", "Para Deletar", true);
            when(repo.findById("d1")).thenReturn(Optional.of(m));
            when(repo.save(any())).thenReturn(m);

            service.delete("d1");

            verify(repo).save(argThat(saved -> !saved.getActive()));
            verify(repo, never()).delete(any()); // nunca deleta fisicamente
        }

        @Test
        @DisplayName("deve lançar exceção ao deletar membro inexistente")
        void deveLancarExcecao() {
            when(repo.findById("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("x"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
