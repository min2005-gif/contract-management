package vn.vatm.contract.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import vn.vatm.contract.integration.adapters.AccountingGateway;
import vn.vatm.contract.integration.adapters.SimulatedAccountingGateway;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T073: accounting reconciliation works and stays internal-only (FR-022). */
class AccountingIntegrationTest extends AbstractIntegrationTest {

  @Autowired private ApplicationContext context;

  @Test
  void reconcileReturnsInternalResult() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-ACC-001");

    mockMvc
        .perform(
            post("/api/v1/integrations/contracts/{id}/accounting/reconcile", id)
                .with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.matched", is(true)))
        .andExpect(jsonPath("$.contractId", is(id)))
        .andExpect(jsonPath("$.accountingReference").exists());
  }

  @Test
  void onlyInternalAccountingGatewayIsWired() {
    // FR-022: exactly one accounting gateway, the internal simulated one — no external
    // public-service/national-database connector is registered.
    var gateways = context.getBeansOfType(AccountingGateway.class);
    assertThat(gateways).hasSize(1);
    assertThat(gateways.values().iterator().next()).isInstanceOf(SimulatedAccountingGateway.class);
  }
}
