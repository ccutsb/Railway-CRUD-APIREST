package com.empresaccutsb.apirest.service;

import com.empresaccutsb.apirest.model.AuditLog;
import com.empresaccutsb.apirest.repository.AuditLogRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            String action,
            String entityType,
            String entityId,
            String actor,
            boolean success,
            String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setActor(actor);
        log.setSuccess(success);
        log.setDetails(details);
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }
}
