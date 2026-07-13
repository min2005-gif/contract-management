package vn.vatm.contract.alert;

/** Conditions the system watches for (FR-016). */
public enum AlertType {
  NEARING_EXPIRY,
  UNSIGNED,
  UNPAID,
  BEHIND_SCHEDULE
}
