package vn.vatm.contract.integration.adapters;

import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.integration.ReconciliationResult;

/** Adapter to VATM's internal accounting software (internal-only, FR-022). */
public interface AccountingGateway {

  ReconciliationResult reconcile(Contract contract);
}
