package vn.vatm.contract.config;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.org.OrganizationalUnit;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.org.User;
import vn.vatm.contract.org.UserRepository;

/**
 * Resolves the authenticated caller into a {@link CurrentUser}, just-in-time provisioning the local
 * {@link User} record from SSO/AD token claims on first sight (T013/T014). The {@code unit} claim
 * (a unit code) determines the user's home unit; roles come from mapped directory groups.
 */
@Service
public class CurrentUserService {

  private final UserRepository users;
  private final OrganizationalUnitRepository units;
  private final vn.vatm.contract.org.UserRoleRepository userRoles;
  private final AuthProperties authProperties;

  public CurrentUserService(
      UserRepository users,
      OrganizationalUnitRepository units,
      vn.vatm.contract.org.UserRoleRepository userRoles,
      AuthProperties authProperties) {
    this.users = users;
    this.units = units;
    this.userRoles = userRoles;
    this.authProperties = authProperties;
  }

  @Transactional
  public CurrentUser require() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new ForbiddenException("Chưa xác thực. / Not authenticated.");
    }
    Set<Role> roles = rolesFromAuthorities(auth);
    User user = users.findByExternalSubject(jwt.getSubject()).orElseGet(() -> provision(jwt));
    // Merge roles the token carries with roles assigned in-app (admin user management).
    userRoles.findByUserId(user.getId()).forEach(ur -> roles.add(ur.getRole()));
    OrganizationalUnit unit = user.getUnit();
    return new CurrentUser(user.getId(), unit.getId(), unit.getCode(), unit.getType(), roles);
  }

  private Set<Role> rolesFromAuthorities(Authentication auth) {
    Set<Role> roles = EnumSet.noneOf(Role.class);
    for (GrantedAuthority ga : auth.getAuthorities()) {
      String name = ga.getAuthority();
      if (name.startsWith("ROLE_")) {
        name = name.substring("ROLE_".length());
      }
      for (Role role : Role.values()) {
        if (role.name().equals(name)) {
          roles.add(role);
        }
      }
    }
    return roles;
  }

  private User provision(Jwt jwt) {
    String unitCode = jwt.getClaimAsString(authProperties.getUnitClaim());
    if (unitCode == null || unitCode.isBlank()) {
      throw new ForbiddenException(
          "Tài khoản chưa gắn với đơn vị. / Account is not associated with a unit.");
    }
    OrganizationalUnit unit =
        units
            .findByCode(unitCode)
            .orElseThrow(
                () ->
                    new ForbiddenException(
                        "Đơn vị không tồn tại: " + unitCode + " / Unknown unit: " + unitCode));
    String name = jwt.getClaimAsString("name");
    String email = jwt.getClaimAsString("email");
    return users.save(new User(jwt.getSubject(), name, email, unit));
  }
}
