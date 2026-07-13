package vn.vatm.contract.workflow;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T041: legal state-machine progression and rejection of illegal transitions (FR-012). */
class WorkflowStateMachineTest extends AbstractIntegrationTest {

  @Test
  void nonOfficialContractBecomesActiveAfterUnitHeadApproval() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WF-001");

    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("PENDING_CHECK")));

    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "CHECK_APPROVE", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  void approvingADraftIsIllegal() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WF-002");

    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "CHECK_APPROVE", null)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }

  @Test
  void tctApprovingBeforeItIsPendingTctIsIllegal() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WF-003");

    performWorkflow(asUser("director", "TCT", Role.ADMIN), id, "TCT_APPROVE", null)
        .andExpect(status().isConflict());
  }

  @Test
  void submittingTwiceIsIllegal() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WF-004");
    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk());

    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isConflict());
  }
}
