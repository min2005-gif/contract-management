package vn.vatm.contract.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T052: consolidated report reconciliation (SC-004) and access control (TCT/management only). */
class ReportSummaryTest extends AbstractIntegrationTest {

  @Test
  void perUnitFiguresReconcileToTotals() throws Exception {
    createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-RPT-001");
    createContract(asUser("bob", "U02", Role.DATA_ENTRY), "HD-RPT-002");

    String json =
        mockMvc
            .perform(get("/api/v1/reports/summary").with(asUser("mgr", "U01", Role.MANAGEMENT)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode report = objectMapper.readTree(json);
    long totalContracts = report.get("totalContracts").asLong();
    BigDecimal totalValue = new BigDecimal(report.get("totalValue").asText());

    long summedCount = 0;
    BigDecimal summedValue = BigDecimal.ZERO;
    for (JsonNode unit : report.get("perUnit")) {
      summedCount += unit.get("contractCount").asLong();
      summedValue = summedValue.add(new BigDecimal(unit.get("totalValue").asText()));
    }

    assertThat(summedCount).isEqualTo(totalContracts);
    assertThat(summedValue.compareTo(totalValue)).isZero();
    assertThat(totalContracts).isGreaterThanOrEqualTo(2);
  }

  @Test
  void tctUserCanViewReport() throws Exception {
    mockMvc
        .perform(get("/api/v1/reports/summary").with(asUser("director", "TCT", Role.ADMIN)))
        .andExpect(status().isOk());
  }

  @Test
  void dataEntryUserIsForbidden() throws Exception {
    mockMvc
        .perform(get("/api/v1/reports/summary").with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isForbidden());
  }
}
