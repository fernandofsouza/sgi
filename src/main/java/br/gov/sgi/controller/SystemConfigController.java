package br.gov.sgi.controller;

import br.gov.sgi.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SystemConfigController {

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
