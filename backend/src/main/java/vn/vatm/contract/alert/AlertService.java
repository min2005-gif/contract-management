package vn.vatm.contract.alert;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.config.AccessControl;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.org.Role;

/** Listing, acknowledgement/resolution, and manual triggering of alerts (US4, FR-017). */
@Service
public class AlertService {

  private final AlertRepository alerts;
  private final AlertEvaluationService evaluationService;
  private final AccessControl accessControl;
  private final CurrentUserService currentUserService;

  public AlertService(
      AlertRepository alerts,
      AlertEvaluationService evaluationService,
      AccessControl accessControl,
      CurrentUserService currentUserService) {
    this.alerts = alerts;
    this.evaluationService = evaluationService;
    this.accessControl = accessControl;
    this.currentUserService = currentUserService;
  }

  @Transactional(readOnly = true)
  public List<AlertResponse> list(AlertType type, AlertStatus status) {
    CurrentUser user = currentUserService.require();
    UUID unitScope = user.canSeeAllUnits() ? null : user.unitId();
    return alerts
        .findAll(filter(unitScope, type, status), Sort.by(Sort.Direction.DESC, "raisedAt"))
        .stream()
        .map(AlertResponse::from)
        .toList();
  }

  @Transactional
  public AlertResponse updateStatus(UUID alertId, AlertStatus newStatus) {
    if (newStatus != AlertStatus.ACKNOWLEDGED && newStatus != AlertStatus.RESOLVED) {
      throw new BadRequestException(
          "Chỉ có thể chuyển sang ACKNOWLEDGED hoặc RESOLVED. / Status must be ACKNOWLEDGED or"
              + " RESOLVED.");
    }
    CurrentUser user = currentUserService.require();
    Alert alert =
        alerts
            .findById(alertId)
            .orElseThrow(
                () -> new NotFoundException("Không tìm thấy cảnh báo. / Alert not found."));
    accessControl.requireUnitAccess(user, alert.getOwningUnitId());
    alert.setStatus(newStatus);
    return AlertResponse.from(alerts.save(alert));
  }

  /** Manually trigger an evaluation pass (admin only). Returns the number of new alerts. */
  @Transactional
  public int triggerEvaluation() {
    CurrentUser user = currentUserService.require();
    if (!user.hasRole(Role.ADMIN)) {
      throw new ForbiddenException(
          "Chỉ quản trị viên mới được kích hoạt đánh giá cảnh báo. / Only an admin may trigger"
              + " alert evaluation.");
    }
    return evaluationService.evaluate();
  }

  private Specification<Alert> filter(UUID unitScope, AlertType type, AlertStatus status) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (unitScope != null) {
        predicates.add(cb.equal(root.get("owningUnitId"), unitScope));
      }
      if (type != null) {
        predicates.add(cb.equal(root.get("type"), type));
      }
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
