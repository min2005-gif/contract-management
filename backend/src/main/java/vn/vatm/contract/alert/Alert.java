package vn.vatm.contract.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A flag raised against a contract for a watched condition (FR-016/FR-017). At most one OPEN alert
 * of a given type may exist per contract (enforced by a partial unique index), which makes
 * evaluation idempotent. {@code owningUnitId} is denormalized for scoped listing.
 */
@Entity
@Table(name = "alert")
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "contract_id", nullable = false)
  private UUID contractId;

  @Column(name = "owning_unit_id", nullable = false)
  private UUID owningUnitId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertStatus status = AlertStatus.OPEN;

  @Column(name = "notified_user_id")
  private UUID notifiedUserId;

  @CreationTimestamp
  @Column(name = "raised_at", updatable = false)
  private Instant raisedAt;

  protected Alert() {}

  public Alert(UUID contractId, UUID owningUnitId, AlertType type, UUID notifiedUserId) {
    this.contractId = contractId;
    this.owningUnitId = owningUnitId;
    this.type = type;
    this.status = AlertStatus.OPEN;
    this.notifiedUserId = notifiedUserId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getContractId() {
    return contractId;
  }

  public UUID getOwningUnitId() {
    return owningUnitId;
  }

  public AlertType getType() {
    return type;
  }

  public AlertStatus getStatus() {
    return status;
  }

  public void setStatus(AlertStatus status) {
    this.status = status;
  }

  public UUID getNotifiedUserId() {
    return notifiedUserId;
  }

  public Instant getRaisedAt() {
    return raisedAt;
  }
}
