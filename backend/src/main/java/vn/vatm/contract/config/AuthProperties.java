package vn.vatm.contract.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Authentication configuration (prefix {@code app.auth}).
 *
 * <p>{@code mode=dev} validates local HS256 tokens (demo). {@code mode=sso} validates RS256 tokens
 * issued by an external OIDC provider — e.g. VATM's IdentityServer at {@code https://sso.vatm.vn} —
 * discovered from {@code issuer-uri}. Claim names and role mapping are configurable so the app can
 * adapt to VATM's token shape without code changes.
 */
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

  /** {@code dev} (local HS256) or {@code sso} (RS256 from an OIDC issuer). */
  private String mode = "dev";

  /** OIDC issuer URI for {@code sso} mode, e.g. {@code https://sso.vatm.vn}. */
  private String issuerUri;

  /** JWT claim carrying the user's organizational-unit code (VATM token shape may differ). */
  private String unitClaim = "unit";

  /** Claims to read roles/groups from; all present are merged. */
  private List<String> roleClaims = new ArrayList<>(List.of("roles", "role", "groups"));

  /**
   * Optional map from an external role/group value to an app role name (DATA_ENTRY, UNIT_HEAD,
   * MANAGEMENT, ADMIN). Values that already equal an app role name map through automatically.
   */
  private Map<String, String> roleMappings = new HashMap<>();

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getIssuerUri() {
    return issuerUri;
  }

  public void setIssuerUri(String issuerUri) {
    this.issuerUri = issuerUri;
  }

  public String getUnitClaim() {
    return unitClaim;
  }

  public void setUnitClaim(String unitClaim) {
    this.unitClaim = unitClaim;
  }

  public List<String> getRoleClaims() {
    return roleClaims;
  }

  public void setRoleClaims(List<String> roleClaims) {
    this.roleClaims = roleClaims;
  }

  public Map<String, String> getRoleMappings() {
    return roleMappings;
  }

  public void setRoleMappings(Map<String, String> roleMappings) {
    this.roleMappings = roleMappings;
  }
}
