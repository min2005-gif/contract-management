package vn.vatm.contract.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consolidated cross-unit report (FR-018). Totals reconcile to the sum of the per-unit breakdown
 * (SC-004).
 */
public record ReportSummary(
    long totalContracts,
    BigDecimal totalValue,
    long nearingExpiry,
    long inProgress,
    List<UnitBreakdown> perUnit) {}
