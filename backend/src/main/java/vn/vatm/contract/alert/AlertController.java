package vn.vatm.contract.alert;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for alerts (US4); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

  private final AlertService service;

  public AlertController(AlertService service) {
    this.service = service;
  }

  @GetMapping
  public List<AlertResponse> list(
      @RequestParam(required = false) AlertType type,
      @RequestParam(required = false) AlertStatus status) {
    return service.list(type, status);
  }

  @PatchMapping("/{alertId}")
  public AlertResponse update(
      @PathVariable UUID alertId, @Valid @RequestBody AlertActionRequest request) {
    return service.updateStatus(alertId, request.status());
  }

  /** Dev/admin trigger to run an evaluation pass immediately. */
  @PostMapping("/evaluate")
  public Map<String, Integer> evaluate() {
    return Map.of("raised", service.triggerEvaluation());
  }
}
