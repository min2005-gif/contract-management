package vn.vatm.contract.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import vn.vatm.contract.org.OrganizationalUnit;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/**
 * T026: required-field validation (FR-011), per-unit uniqueness (FR-026), optimistic concurrency.
 */
class ContractValidationTest extends AbstractIntegrationTest {

  @Autowired private ContractRepository contracts;
  @Autowired private OrganizationalUnitRepository units;

  @Test
  void blankRequiredFieldIsRejected() throws Exception {
    var body = contractBody("HD-VAL-001");
    body.put("name", "");

    mockMvc
        .perform(
            post("/api/v1/contracts")
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void termEndBeforeSignDateIsRejected() throws Exception {
    var body = contractBody("HD-VAL-002");
    body.put("signDate", "2026-06-01");
    body.put("termEnd", "2026-01-01");

    mockMvc
        .perform(
            post("/api/v1/contracts")
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void duplicateContractNumberInSameUnitIsRejected() throws Exception {
    createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-VAL-003");

    mockMvc
        .perform(
            post("/api/v1/contracts")
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contractBody("HD-VAL-003"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"));
  }

  @Test
  void concurrentEditIsRejectedByOptimisticLock() {
    OrganizationalUnit u01 = units.findByCode("U01").orElseThrow();
    Contract c = newContract(u01.getId(), "HD-VAL-004");
    UUID id = contracts.save(c).getId();

    // Two independent loads of the same row (both version 0).
    Contract copyA = contracts.findById(id).orElseThrow();
    Contract copyB = contracts.findById(id).orElseThrow();

    copyA.setName("Cập nhật A");
    contracts.save(copyA); // -> version 1

    copyB.setName("Cập nhật B"); // still version 0
    assertThatThrownBy(() -> contracts.saveAndFlush(copyB))
        .isInstanceOf(OptimisticLockingFailureException.class);

    assertThat(contracts.findById(id).orElseThrow().getName()).isEqualTo("Cập nhật A");
  }

  private Contract newContract(UUID owningUnitId, String number) {
    Contract c = new Contract();
    c.setOwningUnitId(owningUnitId);
    c.setContractNumber(number);
    c.setName("HĐ " + number);
    c.setType(ContractType.SERVICE);
    c.setPartyA("VATM");
    c.setPartyB("Đối tác");
    c.setValue(new BigDecimal("1000000"));
    c.setSignDate(LocalDate.parse("2026-01-01"));
    c.setTermEnd(LocalDate.parse("2026-12-31"));
    c.setPersonInChargeId(UUID.randomUUID());
    c.setStatus(ContractStatus.DRAFT);
    return c;
  }
}
