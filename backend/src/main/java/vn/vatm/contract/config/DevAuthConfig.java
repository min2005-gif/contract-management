package vn.vatm.contract.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Dev-only JWT signing. Enables the {@code dev} profile to mint local HS256 tokens (see {@link
 * DevAuthController}) so the web/mobile clients can authenticate without a real IdP. NOT loaded in
 * production, where identity comes from VATM SSO/AD.
 */
@Configuration
@Profile("dev")
public class DevAuthConfig {

  @Bean
  public JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
    SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }
}
