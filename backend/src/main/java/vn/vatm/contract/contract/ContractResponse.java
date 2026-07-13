package vn.vatm.contract.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Contract representation returned to clients. */
public record ContractResponse(
    UUID id,
    String contractNumber,
    String name,
    ContractType type,
    String partyA,
    String partyB,
    BigDecimal value,
    LocalDate signDate,
    LocalDate termEnd,
    UUID personInChargeId,
    ContractStatus status,
    boolean official,
    UUID owningUnitId,
    boolean signed,
    PaymentStatus paymentStatus,
    int progressPct,
    Map<String, Object> extraFields,
    long version,
    Instant createdAt,
    Instant updatedAt) {

  public static ContractResponse from(Contract c) {
    return new ContractResponse(
        c.getId(),
        c.getContractNumber(),
        c.getName(),
        c.getType(),
        c.getPartyA(),
        c.getPartyB(),
        c.getValue(),
        c.getSignDate(),
        c.getTermEnd(),
        c.getPersonInChargeId(),
        c.getStatus(),
        c.isOfficial(),
        c.getOwningUnitId(),
        c.isSigned(),
        c.getPaymentStatus(),
        c.getProgressPct(),
        c.getExtraFields(),
        c.getVersion(),
        c.getCreatedAt(),
        c.getUpdatedAt());
  }
}
