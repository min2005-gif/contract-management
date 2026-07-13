package vn.vatm.contract.config;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public liveness probe used by quickstart.md (`GET /api/v1/health`). */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  @GetMapping
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }
}
