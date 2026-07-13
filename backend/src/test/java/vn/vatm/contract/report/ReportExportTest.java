package vn.vatm.contract.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** T053: report export to Excel/PDF (FR-019) plus format and access validation. */
class ReportExportTest extends AbstractIntegrationTest {

  @Test
  void exportsXlsx() throws Exception {
    byte[] body =
        mockMvc
            .perform(
                get("/api/v1/reports/export")
                    .param("format", "xlsx")
                    .with(asUser("director", "TCT", Role.ADMIN)))
            .andExpect(status().isOk())
            .andExpect(
                header()
                    .string(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    // .xlsx is a ZIP container → starts with "PK".
    assertThat(body).isNotEmpty();
    assertThat(body[0]).isEqualTo((byte) 'P');
    assertThat(body[1]).isEqualTo((byte) 'K');
  }

  @Test
  void exportsPdf() throws Exception {
    byte[] body =
        mockMvc
            .perform(
                get("/api/v1/reports/export")
                    .param("format", "pdf")
                    .with(asUser("director", "TCT", Role.ADMIN)))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    assertThat(new String(body, 0, 5)).isEqualTo("%PDF-");
  }

  @Test
  void invalidFormatIsRejected() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/reports/export")
                .param("format", "csv")
                .with(asUser("director", "TCT", Role.ADMIN)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void dataEntryUserCannotExport() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/reports/export")
                .param("format", "xlsx")
                .with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isForbidden());
  }
}
