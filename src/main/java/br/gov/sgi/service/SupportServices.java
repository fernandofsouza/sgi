package br.gov.sgi.service;

import br.gov.sgi.dto.AuditLogDTO;
import br.gov.sgi.entity.AuditLog;
import br.gov.sgi.entity.SystemConfig;
import br.gov.sgi.repository.AuditLogRepository;
import br.gov.sgi.repository.SystemConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// ─── AuditLogService ──────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
class AuditLogService {

    private final AuditLogRepository repo;
    private final ObjectMapper        mapper;

    @Transactional
    public void log(AuditLogDTO dto) {
        AuditLog entry = new AuditLog();
        entry.setAction(dto.getAction());
        entry.setEntityType(dto.getEntityType());
        entry.setEntityId(dto.getEntityId());
        entry.setUserId(dto.getUserId());
        entry.setUserName(dto.getUserName());

        if (dto.getDetails() != null) {
            try {
                entry.setDetails(mapper.writeValueAsString(dto.getDetails()));
            } catch (JsonProcessingException e) {
                log.warn("Não foi possível serializar os detalhes do audit log", e);
            }
        }
        repo.save(entry);
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<AuditLog> findByEntity(String entityType, String entityId, Pageable pageable) {
        return repo.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
}

// ─── SystemConfigService ──────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
class SystemConfigService {

    private static final String KEY_CREATION  = "creation_statuses";
    private static final String KEY_PROGRESS  = "progress_statuses";

    private final SystemConfigRepository repo;
    private final ObjectMapper            mapper;

    public Map<String, List<String>> getConfig() {
        List<String> creation = readList(KEY_CREATION);
        List<String> progress = readList(KEY_PROGRESS);
        return Map.of("creationStatuses", creation, "progressStatuses", progress);
    }

    @Transactional
    public Map<String, List<String>> updateConfig(Map<String, List<String>> config) {
        if (config.containsKey("creationStatuses")) {
            saveList(KEY_CREATION, config.get("creationStatuses"));
        }
        if (config.containsKey("progressStatuses")) {
            saveList(KEY_PROGRESS, config.get("progressStatuses"));
        }
        return getConfig();
    }

    private List<String> readList(String key) {
        return repo.findById(key).map(c -> {
            try {
                return mapper.readValue(c.getConfigValue(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                return List.<String>of();
            }
        }).orElse(List.of());
    }

    private void saveList(String key, List<String> values) {
        try {
            String json = mapper.writeValueAsString(values);
            SystemConfig cfg = repo.findById(key).orElse(new SystemConfig());
            cfg.setConfigKey(key);
            cfg.setConfigValue(json);
            repo.save(cfg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar config: " + key, e);
        }
    }
}
