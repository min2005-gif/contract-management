package vn.vatm.contract.integration.adapters;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Placeholder for the real VATM electronic-document integration. Links an internal document
 * reference to a contract without any external calls (FR-022).
 */
@Component
public class SimulatedEDocumentGateway implements EDocumentGateway {

  @Override
  public String link(UUID contractId, String documentRef) {
    return documentRef;
  }
}
