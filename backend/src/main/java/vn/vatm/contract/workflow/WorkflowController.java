package vn.vatm.contract.workflow;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.contract.ContractResponse;

/** REST endpoint for contract workflow actions (US2); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/contracts/{id}/workflow")
public class WorkflowController {

  private final WorkflowService service;

  public WorkflowController(WorkflowService service) {
    this.service = service;
  }

  @PostMapping
  public ContractResponse perform(
      @PathVariable UUID id, @Valid @RequestBody WorkflowActionRequest request) {
    return service.perform(id, request);
  }
}
