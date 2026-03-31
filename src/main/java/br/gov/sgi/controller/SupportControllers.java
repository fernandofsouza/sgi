package br.gov.sgi.controller;

import br.gov.sgi.dto.*;
import br.gov.sgi.entity.AuditLog;
import br.gov.sgi.service.AuditLogService;
import br.gov.sgi.service.RelevanceCriteriaService;
import br.gov.sgi.service.SystemConfigService;
import br.gov.sgi.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

// ─── TeamMemberController ─────────────────────────────────────────────────────
@RestController
@RequestMapping("/team-members")
@RequiredArgsConstructor
class TeamMemberController {

    private final TeamMemberService service;

    @GetMapping
    public ResponseEntity<List<TeamMemberDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamMemberDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamMemberDTO> create(@Valid @RequestBody TeamMemberDTO dto) {
        TeamMemberDTO created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/team-members/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamMemberDTO> update(@PathVariable String id,
                                                 @RequestBody TeamMemberDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// ─── RelevanceCriteriaController ─────────────────────────────────────────────
@RestController
@RequestMapping("/relevance-criteria")
@RequiredArgsConstructor
class RelevanceCriteriaController {

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

// ─── AuditLogController ───────────────────────────────────────────────────────
@RestController
@RequestMapping("/audit-log")
@RequiredArgsConstructor
class AuditLogController {

    private final AuditLogService service;

    @PostMapping
    public ResponseEntity<Void> log(@RequestBody AuditLogDTO dto) {
        service.log(dto);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuditLog>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(service.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/entity/{type}/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuditLog>> findByEntity(
            @PathVariable String type,
            @PathVariable String id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.findByEntity(type, id,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}

// ─── SystemConfigController ───────────────────────────────────────────────────
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
class SystemConfigController {

    private final SystemConfigService service;

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getConfig() {
        return ResponseEntity.ok(service.getConfig());
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, List<String>>> updateConfig(
            @RequestBody Map<String, List<String>> config) {
        return ResponseEntity.ok(service.updateConfig(config));
    }
}
