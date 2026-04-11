package br.gov.sgi.controller;

import br.gov.sgi.dto.RelevanceCriteriaDTO;
import br.gov.sgi.service.RelevanceCriteriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Critérios de Relevância")
@RestController
@RequestMapping("/relevance-criteria")
@RequiredArgsConstructor
public class RelevanceCriteriaController {

    private final RelevanceCriteriaService service;

    @GetMapping
    public ResponseEntity<List<RelevanceCriteriaDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelevanceCriteriaDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RelevanceCriteriaDTO> create(@Valid @RequestBody RelevanceCriteriaDTO dto) {
        RelevanceCriteriaDTO created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/relevance-criteria/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RelevanceCriteriaDTO> update(@PathVariable String id,
                                                        @RequestBody RelevanceCriteriaDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
