package vn.vatm.contract.alert;

import java.time.Instant;
import java.util.UUID;

/** Alert representation returned to clients. */
public record AlertResponse(
    UUID id,
    UUID contractId,
    UUID owningUnitId,
    AlertType type,
    AlertStatus status,
    UUID notifiedUserId,
    Instant raisedAt) {

  public static AlertResponse from(Alert a) {
    return new AlertResponse(
        a.getId(),
        a.getContractId(),
        a.getOwningUnitId(),
        a.getType(),
        a.getStatus(),
        a.getNotifiedUserId(),
        a.getRaisedAt());
  }
}
