package vn.vatm.contract.alert;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.contract.ContractStatus;
import vn.vatm.contract.contract.PaymentStatus;

/**
 * Evaluates the four alert conditions (FR-016) over all non-terminal contracts and raises alerts
 * for responsible users (FR-017). Evaluation is idempotent: an OPEN alert of a given type is only
 * raised once per contract (guarded in code and by a partial unique index). Thresholds are
 * configurable; defaults follow the spec (nearing-expiry = 30 days).
 */
@Service
public class AlertEvaluationService {

  private final ContractRepository contracts;
  private final AlertRepository alerts;
  private final NotificationService notifications;
  private final int nearingExpiryDays;

  public AlertEvaluationService(
      ContractRepository contracts,
      AlertRepository alerts,
      NotificationService notifications,
      @Value("${app.alert.nearing-expiry-days}") int nearingExpiryDays) {
    this.contracts = contracts;
    this.alerts = alerts;
    this.notifications = notifications;
    this.nearingExpiryDays = nearingExpiryDays;
  }

  /** Runs one evaluation pass and returns the number of new alerts raised. */
  @Transactional
  public int evaluate() {
    LocalDate today = LocalDate.now();
    int raised = 0;
    // MVP scans active contracts in memory; a production build would push each rule into SQL.
    for (Contract c : contracts.findByStatusNot(ContractStatus.LIQUIDATED)) {
      if (isNearingExpiry(c, today)) {
        raised += raiseIfAbsent(c, AlertType.NEARING_EXPIRY);
      }
      if (isUnsigned(c, today)) {
        raised += raiseIfAbsent(c, AlertType.UNSIGNED);
      }
      if (isUnpaid(c)) {
        raised += raiseIfAbsent(c, AlertType.UNPAID);
      }
      if (isBehindSchedule(c)) {
        raised += raiseIfAbsent(c, AlertType.BEHIND_SCHEDULE);
      }
    }
    return raised;
  }

  private boolean isNearingExpiry(Contract c, LocalDate today) {
    LocalDate end = c.getTermEnd();
    return end != null && !end.isBefore(today) && !end.isAfter(today.plusDays(nearingExpiryDays));
  }

  private boolean isUnsigned(Contract c, LocalDate today) {
    return !c.isSigned() && c.getSignDate() != null && c.getSignDate().isBefore(today);
  }

  private boolean isUnpaid(Contract c) {
    boolean inExecution =
        c.getStatus() == ContractStatus.ACTIVE
            || c.getStatus() == ContractStatus.IN_PROGRESS
            || c.getStatus() == ContractStatus.COMPLETED;
    return inExecution && c.getPaymentStatus() != PaymentStatus.PAID;
  }

  private boolean isBehindSchedule(Contract c) {
    return c.getProgressPct() < c.getExpectedProgressPct();
  }

  private int raiseIfAbsent(Contract c, AlertType type) {
    if (alerts.existsByContractIdAndTypeAndStatus(c.getId(), type, AlertStatus.OPEN)) {
      return 0;
    }
    Alert alert =
        alerts.save(new Alert(c.getId(), c.getOwningUnitId(), type, c.getPersonInChargeId()));
    notifications.notifyPersonInCharge(alert);
    return 1;
  }
}
