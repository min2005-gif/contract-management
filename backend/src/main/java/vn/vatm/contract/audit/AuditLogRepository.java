package vn.vatm.contract.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, UUID> {

  List<AuditLogEntry> findByEntityIdOrderByCreatedAtAsc(UUID entityId);
}
