package vn.vatm.contract.workflow;

/** Actions that move a contract through its lifecycle (FR-012, FR-013, FR-014). */
public enum WorkflowAction {
  SUBMIT,
  CHECK_APPROVE,
  TCT_APPROVE,
  REJECT,
  LIQUIDATE
}
