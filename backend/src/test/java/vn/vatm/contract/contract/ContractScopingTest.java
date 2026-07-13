package vn.vatm.contract.contract;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T025: owning-unit scoping — a unit cannot read another unit's contract; TCT can. */
class ContractScopingTest extends AbstractIntegrationTest {

  @Test
  void otherUnitCannotReadContract() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-SCOPE-001");

    mockMvc
        .perform(get("/api/v1/contracts/{id}", id).with(asUser("bob", "U02", Role.DATA_ENTRY)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code", is("FORBIDDEN")));
  }

  @Test
  void listIsScopedToCallersUnit() throws Exception {
    createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-SCOPE-002");

    // A U02 user sees none of U01's contracts.
    mockMvc
        .perform(get("/api/v1/contracts").with(asUser("bob", "U02", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total", is(0)));
  }

  @Test
  void tctUserCanReadAnyUnitsContract() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-SCOPE-003");

    mockMvc
        .perform(get("/api/v1/contracts/{id}", id).with(asUser("director", "TCT", Role.MANAGEMENT)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(id)));
  }
}
