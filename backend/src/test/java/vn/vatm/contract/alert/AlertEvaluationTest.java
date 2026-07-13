package vn.vatm.contract.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.contract.PaymentStatus;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.support.AbstractIntegrationTest;
import vn.vatm.contract.support.ContractFixtures;

/** T061: alert evaluation for each of the four conditions plus idempotency (FR-016). */
class AlertEvaluationTest extends AbstractIntegrationTest {

  @Autowired private AlertEvaluationService evaluationService;
  @Autowired private AlertRepository alerts;
  @Autowired private ContractRepository contracts;
  @Autowired private OrganizationalUnitRepository units;

  private UUID u01() {
    return units.findByCode("U01").orElseThrow().getId();
  }

  private UUID persist(Contract c) {
    return contracts.save(c).getId();
  }

  private List<AlertType> typesFor(UUID contractId) {
    return alerts.findByContractId(contractId).stream().map(Alert::getType).toList();
  }

  @Test
  void flagsNearingExpiryOnly() {
    Contract c = ContractFixtures.clean(u01(), "HD-AL-NEAR", UUID.randomUUID());
    c.setTermEnd(LocalDate.now().plusDays(10));
    UUID id = persist(c);

    evaluationService.evaluate();

    assertThat(typesFor(id)).containsExactly(AlertType.NEARING_EXPIRY);
  }

  @Test
  void flagsUnsignedOnly() {
    Contract c = ContractFixtures.clean(u01(), "HD-AL-UNSIGNED", UUID.randomUUID());
    c.setSigned(false);
    c.setSignDate(LocalDate.now().minusDays(1));
    UUID id = persist(c);

    evaluationService.evaluate();

    assertThat(typesFor(id)).containsExactly(AlertType.UNSIGNED);
  }

  @Test
  void flagsUnpaidOnly() {
    Contract c = ContractFixtures.clean(u01(), "HD-AL-UNPAID", UUID.randomUUID());
    c.setPaymentStatus(PaymentStatus.UNPAID);
    UUID id = persist(c);

    evaluationService.evaluate();

    assertThat(typesFor(id)).containsExactly(AlertType.UNPAID);
  }

  @Test
  void flagsBehindScheduleOnly() {
    Contract c = ContractFixtures.clean(u01(), "HD-AL-BEHIND", UUID.randomUUID());
    c.setProgressPct(10);
    c.setExpectedProgressPct(50);
    UUID id = persist(c);

    evaluationService.evaluate();

    assertThat(typesFor(id)).containsExactly(AlertType.BEHIND_SCHEDULE);
  }

  @Test
  void evaluationIsIdempotent() {
    Contract c = ContractFixtures.clean(u01(), "HD-AL-IDEMP", UUID.randomUUID());
    c.setTermEnd(LocalDate.now().plusDays(5));
    UUID id = persist(c);

    evaluationService.evaluate();
    evaluationService.evaluate();
    evaluationService.evaluate();

    assertThat(
            alerts.countByContractIdAndTypeAndStatus(
                id, AlertType.NEARING_EXPIRY, AlertStatus.OPEN))
        .isEqualTo(1);
  }
}
