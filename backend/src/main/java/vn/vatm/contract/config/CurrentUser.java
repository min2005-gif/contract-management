package vn.vatm.contract.config;

import java.util.Set;
import java.util.UUID;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.org.UnitType;

/** The resolved caller for the current request: identity, home unit, and effective roles. */
public record CurrentUser(
    UUID userId, UUID unitId, String unitCode, UnitType unitType, Set<Role> roles) {

  public boolean isTct() {
    return unitType == UnitType.TCT;
  }

  /** TCT users and management/admin roles may see and act across all units (FR-004). */
  public boolean canSeeAllUnits() {
    return isTct() || roles.contains(Role.MANAGEMENT) || roles.contains(Role.ADMIN);
  }

  public boolean hasRole(Role role) {
    return roles.contains(role);
  }
}
