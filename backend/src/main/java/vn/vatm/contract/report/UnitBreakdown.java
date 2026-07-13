package vn.vatm.contract.report;

import java.math.BigDecimal;
import java.util.UUID;

/** Per-unit slice of the consolidated report (FR-018). */
public record UnitBreakdown(
    UUID unitId, String unitName, long contractCount, BigDecimal totalValue) {}
