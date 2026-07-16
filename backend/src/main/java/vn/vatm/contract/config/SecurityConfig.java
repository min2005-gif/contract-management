package vn.vatm.contract.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless security: the API is an OAuth2 resource server. In {@code dev} mode it verifies local
 * HS256 tokens (demo); in {@code sso} mode it verifies RS256 tokens issued by VATM's IdentityServer
 * ({@code app.auth.issuer-uri = https://sso.vatm.vn}), discovering the signing keys via JWKS
 * (FR-005, FR-006). Role/group claims in the token are mapped to application roles. See
 * research.md, Decision 4.
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

  private static final String[] PUBLIC_PATHS = {
    "/api/v1/health",
    "/api/v1/dev/**",
    "/actuator/health",
    "/actuator/health/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html"
  };

  private final GroupRoleMapper groupRoleMapper;
  private final AuthProperties authProperties;
  private final String allowedOrigins;

  public SecurityConfig(
      GroupRoleMapper groupRoleMapper,
      AuthProperties authProperties,
      @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
    this.groupRoleMapper = groupRoleMapper;
    this.authProperties = authProperties;
    this.allowedOrigins = allowedOrigins;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth ->
                oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /** Dev mode: verify local HS256 tokens with a shared secret. */
  @Bean
  @ConditionalOnProperty(name = "app.auth.mode", havingValue = "dev", matchIfMissing = true)
  public JwtDecoder devJwtDecoder(@Value("${app.jwt.secret}") String secret) {
    SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }

  /** SSO mode: verify RS256 tokens from the OIDC issuer (VATM IdentityServer) via JWKS. */
  @Bean
  @ConditionalOnProperty(name = "app.auth.mode", havingValue = "sso")
  public JwtDecoder ssoJwtDecoder() {
    return NimbusJwtDecoder.withIssuerLocation(authProperties.getIssuerUri()).build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::authorities);
    return converter;
  }

  private Collection<GrantedAuthority> authorities(Jwt jwt) {
    List<String> values = new ArrayList<>();
    for (String claim : authProperties.getRoleClaims()) {
      List<String> list = jwt.getClaimAsStringList(claim);
      if (list != null) {
        values.addAll(list);
      } else {
        String single = jwt.getClaimAsString(claim);
        if (single != null) {
          values.add(single);
        }
      }
    }
    List<GrantedAuthority> authorities = new ArrayList<>();
    groupRoleMapper
        .toRoles(values)
        .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));
    return authorities;
  }
}
