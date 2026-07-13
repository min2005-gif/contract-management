package vn.vatm.contract.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Security/operations log entry (FR-025) — distinct from the business audit log. Captures
 * authentication/authorization events and sensitive operations (downloads, exports, admin actions)
 * for security review.
 */
@Entity
@Table(name = "security_event")
public class SecurityEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(nullable = false)
  private String type;

  private String detail;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  protected SecurityEvent() {}

  public SecurityEvent(UUID actorId, String type, String detail) {
    this.actorId = actorId;
    this.type = type;
    this.detail = detail;
  }

  public UUID getId() {
    return id;
  }

  public UUID getActorId() {
    return actorId;
  }

  public String getType() {
    return type;
  }

  public String getDetail() {
    return detail;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
