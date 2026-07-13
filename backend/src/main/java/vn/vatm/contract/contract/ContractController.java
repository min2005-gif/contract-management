package vn.vatm.contract.contract;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for contracts (US1); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

  private final ContractService service;

  public ContractController(ContractService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractInput input) {
    ContractResponse created = service.create(input);
    return ResponseEntity.created(URI.create("/api/v1/contracts/" + created.id())).body(created);
  }

  @GetMapping
  public PageResponse<ContractResponse> search(
      @RequestParam(required = false) UUID unitId,
      @RequestParam(required = false) ContractStatus status,
      @RequestParam(required = false) ContractType type,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.search(unitId, status, type, q, page, size);
  }

  @GetMapping("/{id}")
  public ContractResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public ContractResponse update(@PathVariable UUID id, @Valid @RequestBody ContractInput input) {
    return service.update(id, input);
  }
}
