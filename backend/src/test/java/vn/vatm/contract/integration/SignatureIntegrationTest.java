package vn.vatm.contract.integration;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import vn.vatm.contract.config.ApiExceptions.IntegrationUnavailableException;
import vn.vatm.contract.integration.adapters.SignatureGateway;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/**
 * T072: digital-signature integration — success records status; failure → 502, no partial state.
 */
class SignatureIntegrationTest extends AbstractIntegrationTest {

  @MockBean private SignatureGateway signatureGateway;

  @Test
  void signingRecordsSignedStatusAndDocument() throws Exception {
    when(signatureGateway.sign(any())).thenReturn("SIGNED-REF-123");
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-SIGN-001");

    mockMvc
        .perform(
            post("/api/v1/integrations/contracts/{id}/sign", id)
                .with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.signed", is(true)));
  }

  @Test
  void signatureServiceFailureYields502AndNoPartialState() throws Exception {
    String id = createContract(asUser("alice", "U01", Role.DATA_ENTRY), "HD-SIGN-002");
    when(signatureGateway.sign(any()))
        .thenThrow(new IntegrationUnavailableException("signature service down"));

    mockMvc
        .perform(
            post("/api/v1/integrations/contracts/{id}/sign", id)
                .with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code", is("INTEGRATION_UNAVAILABLE")));

    // The contract must remain unsigned (no partial state).
    mockMvc
        .perform(get("/api/v1/contracts/{id}", id).with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.signed", is(false)));
  }
}
