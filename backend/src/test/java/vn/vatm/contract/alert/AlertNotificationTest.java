package vn.vatm.contract.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.support.AbstractIntegrationTest;
import vn.vatm.contract.support.ContractFixtures;

/** T062: a raised alert is routed to the contract's person in charge (FR-017). */
class AlertNotificationTest extends AbstractIntegrationTest {

  @Autowired private AlertEvaluationService evaluationService;
  @Autowired private AlertRepository alerts;
  @Autowired private ContractRepository contracts;
  @Autowired private OrganizationalUnitRepository units;

  @Test
  void alertIsRoutedToPersonInCharge() {
    UUID personInCharge = UUID.randomUUID();
    UUID unitId = units.findByCode("U01").orElseThrow().getId();
    Contract c = ContractFixtures.clean(unitId, "HD-AL-NOTIFY", personInCharge);
    c.setTermEnd(LocalDate.now().plusDays(7));
    UUID id = contracts.save(c).getId();

    evaluationService.evaluate();

    Alert alert =
        alerts.findByContractId(id).stream()
            .filter(a -> a.getType() == AlertType.NEARING_EXPIRY)
            .findFirst()
            .orElseThrow();
    assertThat(alert.getNotifiedUserId()).isEqualTo(personInCharge);
    assertThat(alert.getOwningUnitId()).isEqualTo(unitId);
  }
}
