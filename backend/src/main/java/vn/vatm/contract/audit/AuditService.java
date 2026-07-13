package vn.vatm.contract.audit;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Emits immutable business-audit records (FR-015). Called from service-layer mutations. */
@Service
public class AuditService {

  private final AuditLogRepository repository;

  public AuditService(AuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void record(
      UUID actorId, String action, String entityType, UUID entityId, Map<String, Object> summary) {
    repository.save(new AuditLogEntry(actorId, action, entityType, entityId, summary));
  }
}
