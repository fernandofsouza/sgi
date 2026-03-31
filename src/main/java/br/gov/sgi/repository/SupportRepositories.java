package br.gov.sgi.repository;

import br.gov.sgi.entity.RelevanceCriteria;
import br.gov.sgi.entity.AuditLog;
import br.gov.sgi.entity.SystemConfig;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
interface RelevanceCriteriaRepository extends JpaRepository<RelevanceCriteria, String> {
    List<RelevanceCriteria> findByActiveTrue();
}

@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);
}

@Repository
interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {}
