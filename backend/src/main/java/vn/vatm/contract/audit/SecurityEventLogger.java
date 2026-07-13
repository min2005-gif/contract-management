package vn.vatm.contract.audit;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records security/operations events such as attachment downloads and report exports (FR-025). */
@Service
public class SecurityEventLogger {

  private final SecurityEventRepository repository;

  public SecurityEventLogger(SecurityEventRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void log(UUID actorId, String type, String detail) {
    repository.save(new SecurityEvent(actorId, type, detail));
  }
}
