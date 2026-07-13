package vn.vatm.contract.workflow;

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
import vn.vatm.contract.contract.ContractStatus;

/** An immutable record of one workflow action taken on a contract (FR-012, FR-014). */
@Entity
@Table(name = "workflow_step")
public class WorkflowStep {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "contract_id", nullable = false)
  private UUID contractId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowAction action;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status", nullable = false)
  private ContractStatus fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", nullable = false)
  private ContractStatus toStatus;

  @Column(name = "actor_id", nullable = false)
  private UUID actorId;

  private String reason;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  protected WorkflowStep() {}

  public WorkflowStep(
      UUID contractId,
      WorkflowAction action,
      ContractStatus fromStatus,
      ContractStatus toStatus,
      UUID actorId,
      String reason) {
    this.contractId = contractId;
    this.action = action;
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.actorId = actorId;
    this.reason = reason;
  }

  public UUID getId() {
    return id;
  }

  public UUID getContractId() {
    return contractId;
  }

  public WorkflowAction getAction() {
    return action;
  }

  public ContractStatus getFromStatus() {
    return fromStatus;
  }

  public ContractStatus getToStatus() {
    return toStatus;
  }

  public UUID getActorId() {
    return actorId;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
