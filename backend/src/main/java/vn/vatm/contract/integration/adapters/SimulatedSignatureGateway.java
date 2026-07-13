package vn.vatm.contract.integration.adapters;

import org.springframework.stereotype.Component;
import vn.vatm.contract.contract.Contract;

/**
 * Placeholder for the real VATM digital-signature integration. It operates purely on internal
 * contract data (no external public-service/national-DB calls, FR-022) and returns a deterministic
 * signed-document reference. Replace with the concrete VATM PKI client when available.
 */
@Component
public class SimulatedSignatureGateway implements SignatureGateway {

  @Override
  public String sign(Contract contract) {
    return "SIGNED-" + contract.getId();
  }
}
