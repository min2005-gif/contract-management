package vn.vatm.contract.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Delivers alert notifications to the responsible user (FR-017). The alert row itself is the in-app
 * notification (surfaced via {@code GET /alerts}); this service additionally dispatches an
 * out-of-band notification (email/push). For the MVP the out-of-band channel is logged; wiring a
 * real mail/push sender is a drop-in replacement here.
 */
@Service
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  public void notifyPersonInCharge(Alert alert) {
    log.info(
        "ALERT {} raised on contract {} → notifying user {}",
        alert.getType(),
        alert.getContractId(),
        alert.getNotifiedUserId());
  }
}
