package vn.vatm.contract.attachment;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T027: attachment upload/download plus content-type and size-limit rejection (FR-010). */
@TestPropertySource(properties = "app.attachment.max-size-bytes=100")
class AttachmentTest extends AbstractIntegrationTest {

  private RequestPostProcessor alice() {
    return asUser("alice", "U01", Role.DATA_ENTRY);
  }

  @Test
  void uploadListAndDownloadRoundTrips() throws Exception {
    String contractId = createContract(alice(), "HD-ATT-001");
    byte[] pdf = "%PDF-1.4 tiny".getBytes();
    MockMultipartFile file = new MockMultipartFile("file", "hopdong.pdf", "application/pdf", pdf);

    mockMvc
        .perform(
            multipart("/api/v1/contracts/{id}/attachments", contractId)
                .file(file)
                .param("kind", "MAIN")
                .with(alice()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.filename").value("hopdong.pdf"))
        .andExpect(jsonPath("$.kind").value("MAIN"));

    mockMvc
        .perform(get("/api/v1/contracts/{id}/attachments", contractId).with(alice()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));

    String listJson =
        mockMvc
            .perform(get("/api/v1/contracts/{id}/attachments", contractId).with(alice()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String attachmentId = objectMapper.readTree(listJson).get(0).get("id").asText();

    mockMvc
        .perform(
            get("/api/v1/contracts/{id}/attachments/{aid}", contractId, attachmentId).with(alice()))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
        .andExpect(content().bytes(pdf));
  }

  @Test
  void disallowedContentTypeIsRejected() throws Exception {
    String contractId = createContract(alice(), "HD-ATT-002");
    MockMultipartFile file =
        new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/contracts/{id}/attachments", contractId).file(file).with(alice()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void oversizeAttachmentIsRejected() throws Exception {
    String contractId = createContract(alice(), "HD-ATT-003");
    byte[] big = new byte[200]; // exceeds the 100-byte test limit
    MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", big);

    mockMvc
        .perform(
            multipart("/api/v1/contracts/{id}/attachments", contractId).file(file).with(alice()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }
}
