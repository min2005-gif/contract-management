package vn.vatm.contract.workflow;

import java.time.Instant;
import java.util.UUID;
import vn.vatm.contract.contract.ContractStatus;

/** A workflow step returned to clients for the status timeline. */
public record WorkflowStepResponse(
    UUID id,
    WorkflowAction action,
    ContractStatus fromStatus,
    ContractStatus toStatus,
    UUID actorId,
    String reason,
    Instant createdAt) {

  public static WorkflowStepResponse from(WorkflowStep s) {
    return new WorkflowStepResponse(
        s.getId(),
        s.getAction(),
        s.getFromStatus(),
        s.getToStatus(),
        s.getActorId(),
        s.getReason(),
        s.getCreatedAt());
  }
}
