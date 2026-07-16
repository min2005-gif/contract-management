package vn.vatm.contract.config;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import vn.vatm.contract.org.Role;

/**
 * Maps external SSO/AD role or group values to application roles (T014). A value maps to a role
 * when either (a) it appears in the configurable {@code app.auth.role-mappings} table, or (b) after
 * stripping common prefixes ({@code ROLE_}, {@code CONTRACT_}, {@code GRP_}) and normalizing case
 * it equals a {@link Role} name. Unknown values are ignored.
 */
@Component
public class GroupRoleMapper {

  private static final List<String> PREFIXES = List.of("ROLE_", "CONTRACT_", "GRP_");

  private final AuthProperties authProperties;

  public GroupRoleMapper(AuthProperties authProperties) {
    this.authProperties = authProperties;
  }

  public Set<Role> toRoles(Collection<String> groups) {
    Set<Role> roles = EnumSet.noneOf(Role.class);
    if (groups == null) {
      return roles;
    }
    for (String group : groups) {
      if (group == null) {
        continue;
      }
      Role mapped = fromMappingTable(group);
      if (mapped != null) {
        roles.add(mapped);
        continue;
      }
      String normalized = strip(group.trim().toUpperCase());
      for (Role role : Role.values()) {
        if (role.name().equals(normalized)) {
          roles.add(role);
        }
      }
    }
    return roles;
  }

  private Role fromMappingTable(String value) {
    String appRole = authProperties.getRoleMappings().get(value);
    if (appRole == null) {
      appRole = authProperties.getRoleMappings().get(value.trim());
    }
    if (appRole == null) {
      return null;
    }
    try {
      return Role.valueOf(appRole.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private String strip(String value) {
    String result = value;
    for (String prefix : PREFIXES) {
      if (result.startsWith(prefix)) {
        result = result.substring(prefix.length());
      }
    }
    return result;
  }
}
