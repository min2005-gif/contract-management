package vn.vatm.contract.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T077: linking an internal electronic document to a contract. */
class EDocumentIntegrationTest extends AbstractIntegrationTest {

  @Test
  void linksDocumentReferenceToContract() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-EDOC-001");

    mockMvc
        .perform(
            post("/api/v1/integrations/contracts/{id}/edocument", id)
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentRef\":\"VPDT-2026-777\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.extraFields.eDocuments", hasItem("VPDT-2026-777")));
  }
}
