package vn.vatm.contract.integration;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.contract.ContractResponse;

/** Links an internal electronic document to a contract (US5). */
@RestController
@RequestMapping("/api/v1/integrations/contracts/{id}/edocument")
public class EDocumentController {

  private final IntegrationService service;

  public EDocumentController(IntegrationService service) {
    this.service = service;
  }

  @PostMapping
  public ContractResponse link(@PathVariable UUID id, @RequestBody LinkRequest request) {
    return service.linkDocument(id, request.documentRef());
  }

  /** Request body carrying the internal document reference to link. */
  public record LinkRequest(@NotBlank String documentRef) {}
}
