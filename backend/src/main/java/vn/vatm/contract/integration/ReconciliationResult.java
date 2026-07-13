package vn.vatm.contract.integration;

import java.math.BigDecimal;
import java.util.UUID;

/** Result of reconciling a contract's financials against the internal accounting system. */
public record ReconciliationResult(
    UUID contractId,
    BigDecimal contractValue,
    String accountingReference,
    boolean matched,
    String message) {}
