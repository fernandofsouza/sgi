package br.gov.sgi.controller;

import br.gov.sgi.dto.*;
import br.gov.sgi.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final IndicatorService indicatorService;

    @GetMapping
    public ResponseEntity<Page<IndicatorSummaryDTO>> listAll(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String creationStatus,
            @RequestParam(required = false) String progressStatus,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("seqId"));
        return ResponseEntity.ok(indicatorService.findAll(year, range, label, creationStatus, progressStatus, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndicatorDetailDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(indicatorService.findById(id));
    }

    @GetMapping("/roots")
    public ResponseEntity<List<IndicatorSummaryDTO>> getRoots() {
        return ResponseEntity.ok(indicatorService.findRoots());
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<IndicatorSummaryDTO>> getChildren(@PathVariable String id) {
        return ResponseEntity.ok(indicatorService.findChildren(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndicatorDetailDTO> create(@Valid @RequestBody CreateIndicatorDTO dto) {
        IndicatorDetailDTO created = indicatorService.create(dto);
        return ResponseEntity.created(URI.create("/api/indicators/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndicatorDetailDTO> update(@PathVariable String id,
                                                     @Valid @RequestBody UpdateIndicatorDTO dto) {
        return ResponseEntity.ok(indicatorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        indicatorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Check-ins ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/checkins")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckInDTO> addCheckIn(@PathVariable String id,
                                                  @Valid @RequestBody CreateCheckInDTO dto) {
        CheckInDTO checkIn = indicatorService.addCheckIn(id, dto);
        return ResponseEntity.created(URI.create("/api/indicators/" + id + "/checkins/" + checkIn.getId()))
                             .body(checkIn);
    }

    @GetMapping("/{id}/checkins")
    public ResponseEntity<List<CheckInDTO>> listCheckIns(@PathVariable String id) {
        return ResponseEntity.ok(indicatorService.listCheckIns(id));
    }

    // ── Avaliações de Relevância ─────────────────────────────────────────────

    @PutMapping("/{id}/relevance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateRelevance(@PathVariable String id,
                                                 @RequestBody List<RelevanceAssessmentDTO> assessments) {
        indicatorService.updateRelevanceAssessments(id, assessments);
        return ResponseEntity.noContent().build();
    }
}
