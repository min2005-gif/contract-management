package vn.vatm.contract.contract;

/** Contract lifecycle status (FR-012). */
public enum ContractStatus {
  DRAFT,
  PENDING_CHECK,
  PENDING_TCT_APPROVAL,
  ACTIVE,
  IN_PROGRESS,
  COMPLETED,
  LIQUIDATED
}
