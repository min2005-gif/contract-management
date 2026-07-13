package vn.vatm.contract.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;
import vn.vatm.contract.support.ContractFixtures;

/** T067/T069: the evaluate trigger endpoint, scoped listing, and acknowledge. */
class AlertApiTest extends AbstractIntegrationTest {

  @Autowired private ContractRepository contracts;
  @Autowired private OrganizationalUnitRepository units;

  private UUID seedNearingContract(String number) {
    UUID unitId = units.findByCode("U01").orElseThrow().getId();
    Contract c = ContractFixtures.clean(unitId, number, UUID.randomUUID());
    c.setTermEnd(LocalDate.now().plusDays(9));
    return contracts.save(c).getId();
  }

  private void evaluateAsAdmin() throws Exception {
    mockMvc
        .perform(post("/api/v1/alerts/evaluate").with(asUser("root", "TCT", Role.ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.raised").exists());
  }

  @Test
  void evaluateEndpointRequiresAdmin() throws Exception {
    mockMvc
        .perform(post("/api/v1/alerts/evaluate").with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isForbidden());

    evaluateAsAdmin();
  }

  @Test
  void listShowsUnitAlertsAndAcknowledgeWorks() throws Exception {
    UUID contractId = seedNearingContract("HD-ALAPI-001");
    evaluateAsAdmin();

    String json =
        mockMvc
            .perform(get("/api/v1/alerts").with(asUser("head", "U01", Role.UNIT_HEAD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String alertId = null;
    for (JsonNode alert : objectMapper.readTree(json)) {
      if (alert.get("contractId").asText().equals(contractId.toString())) {
        alertId = alert.get("id").asText();
      }
    }
    assertThat(alertId).as("U01 head should see the U01 contract's alert").isNotNull();

    mockMvc
        .perform(
            patch("/api/v1/alerts/{id}", alertId)
                .with(asUser("head", "U01", Role.UNIT_HEAD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACKNOWLEDGED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
  }

  @Test
  void otherUnitDoesNotSeeThisContractsAlert() throws Exception {
    UUID contractId = seedNearingContract("HD-ALAPI-002");
    evaluateAsAdmin();

    String json =
        mockMvc
            .perform(get("/api/v1/alerts").with(asUser("bob", "U02", Role.UNIT_HEAD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    for (JsonNode alert : objectMapper.readTree(json)) {
      assertThat(alert.get("contractId").asText()).isNotEqualTo(contractId.toString());
    }
  }
}
