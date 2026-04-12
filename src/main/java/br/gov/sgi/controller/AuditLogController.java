package br.gov.sgi.controller;

import br.gov.sgi.dto.AuditLogDTO;
import br.gov.sgi.entity.AuditLog;
import br.gov.sgi.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit Log")
@RestController
@RequestMapping("/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

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
