package vn.vatm.contract.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/**
 * T042: role rules, the official→TCT-approval path, reject-with-reason, and audit trail
 * (FR-013/14).
 */
class WorkflowRulesTest extends AbstractIntegrationTest {

  @Autowired private WorkflowStepRepository steps;

  @Test
  void officialContractRequiresTctApproval() throws Exception {
    String id = createOfficialContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WFR-001");
    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk());

    // Unit-head approval moves an official contract to PENDING_TCT_APPROVAL, not ACTIVE.
    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "CHECK_APPROVE", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("PENDING_TCT_APPROVAL")));

    // A subordinate unit head cannot grant final approval.
    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "TCT_APPROVE", null)
        .andExpect(status().isForbidden());

    // TCT approver finalizes it.
    performWorkflow(asUser("director", "TCT", Role.ADMIN), id, "TCT_APPROVE", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("ACTIVE")));

    assertThat(steps.findByContractIdOrderByCreatedAtAsc(UUID.fromString(id))).hasSize(3);
  }

  @Test
  void dataEntryCannotApprove() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WFR-002");
    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk());

    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "CHECK_APPROVE", null)
        .andExpect(status().isForbidden());
  }

  @Test
  void otherUnitHeadCannotApprove() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WFR-003");
    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk());

    // A unit head from U02 has no access to a U01 contract.
    performWorkflow(asUser("head2", "U02", Role.UNIT_HEAD), id, "CHECK_APPROVE", null)
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectRequiresReasonAndReturnsToDraft() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-WFR-004");
    performWorkflow(asUser("alice", "U01", Role.DATA_ENTRY), id, "SUBMIT", null)
        .andExpect(status().isOk());

    // Reject without a reason is a bad request.
    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "REJECT", null)
        .andExpect(status().isBadRequest());

    // Reject with a reason returns the contract to DRAFT.
    performWorkflow(asUser("head", "U01", Role.UNIT_HEAD), id, "REJECT", "Thiếu phụ lục")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("DRAFT")));
  }
}
