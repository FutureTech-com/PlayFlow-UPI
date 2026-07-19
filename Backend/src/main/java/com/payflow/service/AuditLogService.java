package com.payflow.service;

import com.payflow.entity.AuditLog;
import com.payflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

	public void log(String userId, String action, String entityType, String entityId, String details) {
        AuditLog logEntry = new AuditLog();
        logEntry.setUserId(userId);
        logEntry.setAction(action);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setDetails(details);
        auditLogRepository.save(logEntry);
    }
}
