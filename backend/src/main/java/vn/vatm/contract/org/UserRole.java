package vn.vatm.contract.org;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A role granted to a user, optionally scoped to a unit (null scope = TCT-wide, e.g. management or
 * admin). Roles are normally sourced from the SSO/AD token per request; this table records
 * administratively assigned roles for reporting and break-glass access.
 */
@Entity
@Table(name = "user_role")
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "scope_unit_id")
  private UUID scopeUnitId;

  protected UserRole() {}

  public UserRole(UUID userId, Role role, UUID scopeUnitId) {
    this.userId = userId;
    this.role = role;
    this.scopeUnitId = scopeUnitId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public Role getRole() {
    return role;
  }

  public UUID getScopeUnitId() {
    return scopeUnitId;
  }
}
