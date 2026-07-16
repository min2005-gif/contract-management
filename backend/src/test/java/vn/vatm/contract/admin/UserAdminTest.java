package vn.vatm.contract.admin;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.support.AbstractIntegrationTest;

/** Admin user management: admin can list/create; non-admins are forbidden; roles take effect. */
class UserAdminTest extends AbstractIntegrationTest {

  @Test
  void adminCanCreateAndListUsers() throws Exception {
    String body =
        "{\"externalSubject\":\"nv.moi.u01\",\"fullName\":\"Nguyễn Văn A\","
            + "\"email\":\"a@vatm.vn\",\"unitCode\":\"U01\",\"active\":true,"
            + "\"roles\":[\"DATA_ENTRY\",\"UNIT_HEAD\"]}";

    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .with(asUser("admin.tct", "TCT", Role.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.externalSubject").value("nv.moi.u01"))
        .andExpect(jsonPath("$.unitCode").value("U01"))
        .andExpect(jsonPath("$.roles", hasItem("UNIT_HEAD")));

    mockMvc
        .perform(get("/api/v1/admin/users").with(asUser("admin.tct", "TCT", Role.ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].externalSubject", hasItem("nv.moi.u01")));
  }

  @Test
  void nonAdminIsForbidden() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/users").with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isForbidden());
  }

  @Test
  void unitsAreListedForDropdowns() throws Exception {
    mockMvc
        .perform(get("/api/v1/units").with(asUser("alice", "U01", Role.DATA_ENTRY)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].code", hasItem("TCT")))
        .andExpect(jsonPath("$[*].code", hasItem("U01")));
  }
}
