package vn.vatm.contract.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractStatus;
import vn.vatm.contract.contract.ContractType;
import vn.vatm.contract.contract.PaymentStatus;

/**
 * Builds {@link Contract} entities for tests that need direct control over fields the create API
 * does not expose (signed / paymentStatus / progress). The default is a "clean" ACTIVE contract
 * that triggers no alerts; tests flip one field to exercise a single condition.
 */
public final class ContractFixtures {

  private ContractFixtures() {}

  public static Contract clean(UUID owningUnitId, String number, UUID personInChargeId) {
    Contract c = new Contract();
    c.setOwningUnitId(owningUnitId);
    c.setContractNumber(number);
    c.setName("HĐ " + number);
    c.setType(ContractType.SERVICE);
    c.setPartyA("VATM");
    c.setPartyB("Đối tác");
    c.setValue(new BigDecimal("1000000"));
    c.setSignDate(LocalDate.now().minusDays(30));
    c.setTermEnd(LocalDate.now().plusYears(1));
    c.setPersonInChargeId(personInChargeId);
    c.setStatus(ContractStatus.ACTIVE);
    c.setSigned(true);
    c.setPaymentStatus(PaymentStatus.PAID);
    c.setProgressPct(100);
    c.setExpectedProgressPct(100);
    return c;
  }
}
