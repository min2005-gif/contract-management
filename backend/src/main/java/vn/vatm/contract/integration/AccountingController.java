package vn.vatm.contract.integration;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Accounting reconciliation endpoint (US5); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/integrations/contracts/{id}/accounting")
public class AccountingController {

  private final IntegrationService service;

  public AccountingController(IntegrationService service) {
    this.service = service;
  }

  @PostMapping("/reconcile")
  public ReconciliationResult reconcile(@PathVariable UUID id) {
    return service.reconcile(id);
  }
}
