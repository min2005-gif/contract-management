package vn.vatm.contract.contract;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T024: contract API contract test for POST/GET/PUT against the OpenAPI shape. */
class ContractApiContractTest extends AbstractIntegrationTest {

  @Test
  void createReturnsDraftContractOwnedByCallersUnit() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/contracts")
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contractBody("HD-API-001"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.contractNumber", is("HD-API-001")))
        .andExpect(jsonPath("$.status", is("DRAFT")))
        .andExpect(jsonPath("$.owningUnitId", notNullValue()))
        .andExpect(jsonPath("$.version", is(0)));
  }

  @Test
  void getReturnsPreviouslyCreatedContract() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-API-002");

    mockMvc
        .perform(get("/api/v1/contracts/{id}", id).with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(id)))
        .andExpect(jsonPath("$.contractNumber", is("HD-API-002")));
  }

  @Test
  void updateModifiesFieldsAndBumpsVersion() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-API-003");
    var body = contractBody("HD-API-003");
    body.put("name", "Tên đã cập nhật");

    mockMvc
        .perform(
            put("/api/v1/contracts/{id}", id)
                .with(asUser("alice", "U01", Role.DATA_ENTRY))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Tên đã cập nhật")))
        .andExpect(jsonPath("$.version", is(1)));
  }
}
