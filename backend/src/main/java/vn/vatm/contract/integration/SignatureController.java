package vn.vatm.contract.integration;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.contract.ContractResponse;

/** Digital-signature integration endpoint (US5); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/integrations/contracts/{id}")
public class SignatureController {

  private final IntegrationService service;

  public SignatureController(IntegrationService service) {
    this.service = service;
  }

  @PostMapping("/sign")
  public ContractResponse sign(@PathVariable UUID id) {
    return service.sign(id);
  }
}
