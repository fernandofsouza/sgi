package br.gov.sgi.service;

import br.gov.sgi.dto.AuditLogDTO;
import br.gov.sgi.entity.AuditLog;
import br.gov.sgi.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

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
