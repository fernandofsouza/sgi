package br.gov.sgi.controller;

import br.gov.sgi.config.SecurityConfig;
import br.gov.sgi.dto.*;
import br.gov.sgi.service.IndicatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndicatorController.class)
@Import(SecurityConfig.class)
@DisplayName("IndicatorController")
class IndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IndicatorService indicatorService;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private IndicatorSummaryDTO buildSummary(String id, String title) {
        return IndicatorSummaryDTO.builder()
                .id(id).seqId(1).title(title)
                .creationStatus("Aprovado").progressStatus("Em andamento normal")
                .progress(50).referenceYear(2026)
                .referenceRange("semestral").referenceLabel("1S")
                .assignees(List.of()).checkInCount(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private IndicatorDetailDTO buildDetail(String id, String title) {
        return IndicatorDetailDTO.builder()
                .id(id).seqId(1).title(title)
                .creationStatus("Aprovado").progressStatus("Em andamento normal")
                .progress(50).referenceYear(2026)
                .referenceRange("semestral").referenceLabel("1S")
                .assignees(List.of()).checkIns(List.of())
                .criteria(List.of()).achievementScale(List.of())
                .relevanceAssessments(List.of()).childrenIds(List.of())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    // ── GET /indicators ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /indicators")
    class ListAll {

        @Test
        @DisplayName("deve retornar 200 com página de indicadores")
        void deveRetornar200() throws Exception {
            Page<IndicatorSummaryDTO> page = new PageImpl<>(List.of(buildSummary("1", "Teste")));
            when(indicatorService.findAll(any(), any(), any(), any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/indicators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value("1"))
                    .andExpect(jsonPath("$.content[0].title").value("Teste"));
        }

        @Test
        @DisplayName("deve aceitar parâmetros de filtro")
        void deveAceitarFiltros() throws Exception {
            when(indicatorService.findAll(eq(2026), eq("semestral"), eq("1S"), any(), any(), any()))
                    .thenReturn(Page.empty());

            mockMvc.perform(get("/indicators")
                            .param("year", "2026")
                            .param("range", "semestral")
                            .param("label", "1S"))
                    .andExpect(status().isOk());

            verify(indicatorService).findAll(eq(2026), eq("semestral"), eq("1S"), any(), any(), any());
        }
    }

    // ── GET /indicators/{id} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /indicators/{id}")
    class GetById {

        @Test
        @DisplayName("deve retornar 200 com detalhe do indicador")
        void deveRetornar200() throws Exception {
            when(indicatorService.findById("abc")).thenReturn(buildDetail("abc", "Detalhe"));

            mockMvc.perform(get("/indicators/abc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("abc"))
                    .andExpect(jsonPath("$.title").value("Detalhe"));
        }

        @Test
        @DisplayName("deve retornar 404 quando indicador não existe")
        void deveRetornar404() throws Exception {
            when(indicatorService.findById("nao-existe"))
                    .thenThrow(new EntityNotFoundException("Indicador não encontrado: nao-existe"));

            mockMvc.perform(get("/indicators/nao-existe"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET /indicators/roots ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /indicators/roots")
    class GetRoots {

        @Test
        @DisplayName("deve retornar lista de indicadores raiz")
        void deveRetornarRaizes() throws Exception {
            when(indicatorService.findRoots()).thenReturn(
                    List.of(buildSummary("r1", "Raiz 1"), buildSummary("r2", "Raiz 2"))
            );

            mockMvc.perform(get("/indicators/roots"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // ── POST /indicators ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /indicators")
    class Create {

        @Test
        @DisplayName("deve retornar 201 com Location header ao criar indicador")
        void deveRetornar201() throws Exception {
            CreateIndicatorDTO dto = new CreateIndicatorDTO();
            dto.setTitle("Novo Indicador");
            dto.setReferenceYear(2026);
            dto.setReferenceRange("anual");
            dto.setReferenceLabel("Anual");

            IndicatorDetailDTO created = buildDetail("novo-id", "Novo Indicador");
            when(indicatorService.create(any())).thenReturn(created);

            mockMvc.perform(post("/indicators")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/indicators/novo-id"))
                    .andExpect(jsonPath("$.id").value("novo-id"));
        }

        @Test
        @DisplayName("deve retornar 400 quando título não informado")
        void deveRetornar400SemTitulo() throws Exception {
            CreateIndicatorDTO dto = new CreateIndicatorDTO();
            // title não informado

            mockMvc.perform(post("/indicators")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── PUT /indicators/{id} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /indicators/{id}")
    class Update {

        @Test
        @DisplayName("deve retornar 200 ao atualizar indicador")
        void deveRetornar200() throws Exception {
            UpdateIndicatorDTO dto = new UpdateIndicatorDTO();
            dto.setTitle("Título Atualizado");

            when(indicatorService.update(eq("upd-id"), any())).thenReturn(buildDetail("upd-id", "Título Atualizado"));

            mockMvc.perform(put("/indicators/upd-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Título Atualizado"));
        }
    }

    // ── DELETE /indicators/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /indicators/{id}")
    class DeleteIndicator {

        @Test
        @DisplayName("deve retornar 204 ao deletar indicador")
        void deveRetornar204() throws Exception {
            doNothing().when(indicatorService).delete("del-id");

            mockMvc.perform(delete("/indicators/del-id"))
                    .andExpect(status().isNoContent());

            verify(indicatorService).delete("del-id");
        }

        @Test
        @DisplayName("deve retornar 404 ao deletar indicador inexistente")
        void deveRetornar404() throws Exception {
            doThrow(new EntityNotFoundException("não encontrado")).when(indicatorService).delete("x");

            mockMvc.perform(delete("/indicators/x"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── POST /indicators/{id}/checkins ────────────────────────────────────────

    @Nested
    @DisplayName("POST /indicators/{id}/checkins")
    class AddCheckIn {

        @Test
        @DisplayName("deve retornar 201 ao registrar check-in")
        void deveRetornar201() throws Exception {
            CreateCheckInDTO dto = new CreateCheckInDTO(
                    LocalDate.now(), 75, "Progresso positivo", "m1", List.of()
            );
            CheckInDTO checkIn = CheckInDTO.builder()
                    .id("ck-1").indicatorId("ind-1").progress(75)
                    .notes("Progresso positivo").criteriaUpdates(List.of())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(indicatorService.addCheckIn(eq("ind-1"), any())).thenReturn(checkIn);

            mockMvc.perform(post("/indicators/ind-1/checkins")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("ck-1"))
                    .andExpect(jsonPath("$.progress").value(75));
        }
    }
}
