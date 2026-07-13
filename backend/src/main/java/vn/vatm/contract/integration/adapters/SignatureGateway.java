package vn.vatm.contract.integration.adapters;

import vn.vatm.contract.contract.Contract;

/**
 * Adapter to VATM's internal digital-signature service. Implementations must be internal-only
 * (FR-022) and throw {@code IntegrationUnavailableException} when the service cannot be reached.
 */
public interface SignatureGateway {

  /** Signs the contract and returns a reference to the signed document. */
  String sign(Contract contract);
}
