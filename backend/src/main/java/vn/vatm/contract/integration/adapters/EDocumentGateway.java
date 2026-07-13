package vn.vatm.contract.integration.adapters;

import java.util.UUID;

/** Adapter to VATM's internal electronic-document system (internal-only, FR-022). */
public interface EDocumentGateway {

  /** Links an existing internal document to the contract and returns the stored reference. */
  String link(UUID contractId, String documentRef);
}
