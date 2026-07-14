package vn.vatm.contract.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only endpoint that issues a local HS256 token for a chosen user/unit/roles, standing in for
 * VATM SSO/AD during development. Active only under the {@code dev} profile.
 */
@RestController
@RequestMapping("/api/v1/dev")
@Profile("dev")
public class DevAuthController {

  private final JwtEncoder encoder;

  public DevAuthController(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  @PostMapping("/token")
  public TokenResponse token(@RequestBody TokenRequest request) {
    Instant now = Instant.now();
    String name = request.name() != null ? request.name() : request.subject();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(request.subject())
            .issuedAt(now)
            .expiresAt(now.plus(8, ChronoUnit.HOURS))
            .claim("unit", request.unit())
            .claim("groups", request.roles() == null ? List.of() : request.roles())
            .claim("name", name)
            .claim("email", request.subject() + "@vatm.vn")
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new TokenResponse(token, name, request.unit(), request.roles());
  }

  public record TokenRequest(String subject, String unit, List<String> roles, String name) {}

  public record TokenResponse(String token, String name, String unit, List<String> roles) {}
}
