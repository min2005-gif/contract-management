package vn.vatm.contract.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless security: the API is an OAuth2 resource server that trusts JWTs issued by VATM's SSO/AD
 * federation (FR-005, FR-006). Directory groups in the token are mapped to application roles.
 *
 * <p>For local/dev/test the token signature is verified with a symmetric secret ({@code
 * app.jwt.secret}); a production deployment swaps in the IdP's JWKS via {@code
 * spring.security.oauth2.resourceserver.jwt.issuer-uri}. See research.md, Decision 4.
 */
@Configuration
public class SecurityConfig {

  private static final String[] PUBLIC_PATHS = {
    "/api/v1/health",
    "/actuator/health",
    "/actuator/health/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html"
  };

  private final GroupRoleMapper groupRoleMapper;

  public SecurityConfig(GroupRoleMapper groupRoleMapper) {
    this.groupRoleMapper = groupRoleMapper;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth ->
                oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
    SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::authorities);
    return converter;
  }

  private Collection<GrantedAuthority> authorities(Jwt jwt) {
    List<String> groups = new ArrayList<>();
    List<String> groupsClaim = jwt.getClaimAsStringList("groups");
    if (groupsClaim != null) {
      groups.addAll(groupsClaim);
    }
    List<String> rolesClaim = jwt.getClaimAsStringList("roles");
    if (rolesClaim != null) {
      groups.addAll(rolesClaim);
    }
    List<GrantedAuthority> authorities = new ArrayList<>();
    groupRoleMapper
        .toRoles(groups)
        .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));
    return authorities;
  }
}
