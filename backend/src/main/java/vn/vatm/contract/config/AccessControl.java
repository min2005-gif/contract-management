package vn.vatm.contract.config;

import java.util.UUID;
import org.springframework.stereotype.Component;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;

/** Central owning-unit access checks (T015). */
@Component
public class AccessControl {

  public boolean canAccessUnit(CurrentUser user, UUID owningUnitId) {
    return user.canSeeAllUnits() || user.unitId().equals(owningUnitId);
  }

  public void requireUnitAccess(CurrentUser user, UUID owningUnitId) {
    if (!canAccessUnit(user, owningUnitId)) {
      throw new ForbiddenException(
          "Không thể truy cập dữ liệu của đơn vị khác. / Cannot access another unit's data.");
    }
  }
}
