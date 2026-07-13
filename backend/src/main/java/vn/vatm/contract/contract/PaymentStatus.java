package vn.vatm.contract.contract;

/** Payment state, used by the "unpaid" alert (FR-016). */
public enum PaymentStatus {
  UNPAID,
  PARTIAL,
  PAID
}
