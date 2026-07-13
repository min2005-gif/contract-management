package vn.vatm.contract.config;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import vn.vatm.contract.org.Role;

/**
 * Maps VATM SSO/AD directory group names to application roles (T014). A group maps to a role when,
 * after stripping common prefixes ({@code ROLE_}, {@code CONTRACT_}, {@code GRP_}) and normalizing
 * case, it equals a {@link Role} name. Unknown groups are ignored.
 */
@Component
public class GroupRoleMapper {

  private static final List<String> PREFIXES = List.of("ROLE_", "CONTRACT_", "GRP_");

  public Set<Role> toRoles(Collection<String> groups) {
    Set<Role> roles = EnumSet.noneOf(Role.class);
    if (groups == null) {
      return roles;
    }
    for (String group : groups) {
      if (group == null) {
        continue;
      }
      String normalized = group.trim().toUpperCase();
      for (String prefix : PREFIXES) {
        if (normalized.startsWith(prefix)) {
          normalized = normalized.substring(prefix.length());
        }
      }
      for (Role role : Role.values()) {
        if (role.name().equals(normalized)) {
          roles.add(role);
        }
      }
    }
    return roles;
  }
}
