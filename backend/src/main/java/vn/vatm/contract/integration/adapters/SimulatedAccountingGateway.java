package vn.vatm.contract.integration.adapters;

import org.springframework.stereotype.Component;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.integration.ReconciliationResult;

/**
 * Placeholder for the real VATM accounting integration. Reconciles using only internal contract
 * data — it makes no external public-service/national-DB calls (FR-022).
 */
@Component
public class SimulatedAccountingGateway implements AccountingGateway {

  @Override
  public ReconciliationResult reconcile(Contract contract) {
    return new ReconciliationResult(
        contract.getId(),
        contract.getValue(),
        "ACC-" + contract.getId(),
        true,
        "Đối chiếu với hệ thống kế toán nội bộ. / Reconciled against internal accounting.");
  }
}
