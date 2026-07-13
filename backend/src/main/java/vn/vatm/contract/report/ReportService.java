package vn.vatm.contract.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.audit.SecurityEventLogger;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.contract.ContractStatus;
import vn.vatm.contract.org.OrganizationalUnit;
import vn.vatm.contract.org.OrganizationalUnitRepository;

/**
 * Produces the consolidated cross-unit report for TCT/management (FR-018) and exports it to
 * Excel/PDF (FR-019). Access is limited to users who can see all units; totals reconcile to the
 * per-unit breakdown (SC-004). Exports emit a security event (FR-025).
 */
@Service
public class ReportService {

  private final ContractRepository contracts;
  private final OrganizationalUnitRepository units;
  private final CurrentUserService currentUserService;
  private final ExcelExporter excelExporter;
  private final PdfExporter pdfExporter;
  private final SecurityEventLogger securityLog;
  private final int nearingExpiryDays;

  public ReportService(
      ContractRepository contracts,
      OrganizationalUnitRepository units,
      CurrentUserService currentUserService,
      ExcelExporter excelExporter,
      PdfExporter pdfExporter,
      SecurityEventLogger securityLog,
      @Value("${app.report.nearing-expiry-days}") int nearingExpiryDays) {
    this.contracts = contracts;
    this.units = units;
    this.currentUserService = currentUserService;
    this.excelExporter = excelExporter;
    this.pdfExporter = pdfExporter;
    this.securityLog = securityLog;
    this.nearingExpiryDays = nearingExpiryDays;
  }

  @Transactional(readOnly = true)
  public ReportSummary summary() {
    requireReportAccess(currentUserService.require());
    return build();
  }

  @Transactional
  public ExportFile export(String format) {
    CurrentUser user = currentUserService.require();
    requireReportAccess(user);
    ReportSummary summary = build();
    ExportFile file =
        switch (format == null ? "" : format.toLowerCase()) {
          case "xlsx" ->
              new ExportFile(
                  "bao-cao-hop-dong.xlsx",
                  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                  excelExporter.toXlsx(summary));
          case "pdf" ->
              new ExportFile("bao-cao-hop-dong.pdf", "application/pdf", pdfExporter.toPdf(summary));
          default ->
              throw new BadRequestException(
                  "Định dạng không hợp lệ (xlsx|pdf). / Invalid format (xlsx|pdf).");
        };
    securityLog.log(user.userId(), "REPORT_EXPORT", "format=" + format);
    return file;
  }

  private ReportSummary build() {
    long totalContracts = contracts.count();
    BigDecimal totalValue = contracts.sumAllValue();

    LocalDate today = LocalDate.now();
    long nearingExpiry = contracts.countByTermEndBetween(today, today.plusDays(nearingExpiryDays));
    long inProgress =
        contracts.countByStatusIn(EnumSet.of(ContractStatus.ACTIVE, ContractStatus.IN_PROGRESS));

    Map<UUID, String> unitNames =
        units.findAll().stream()
            .collect(Collectors.toMap(OrganizationalUnit::getId, OrganizationalUnit::getName));

    List<UnitBreakdown> perUnit = new ArrayList<>();
    for (Object[] row : contracts.aggregateByUnit()) {
      UUID unitId = (UUID) row[0];
      long count = ((Number) row[1]).longValue();
      BigDecimal sum = (BigDecimal) row[2];
      perUnit.add(new UnitBreakdown(unitId, unitNames.getOrDefault(unitId, "?"), count, sum));
    }

    return new ReportSummary(totalContracts, totalValue, nearingExpiry, inProgress, perUnit);
  }

  private void requireReportAccess(CurrentUser user) {
    if (!user.canSeeAllUnits()) {
      throw new ForbiddenException(
          "Chỉ TCT/lãnh đạo mới xem được báo cáo tổng hợp. / Only TCT/management may view the"
              + " consolidated report.");
    }
  }

  /** A generated export file. */
  public record ExportFile(String filename, String contentType, byte[] content) {}
}
