package vn.vatm.contract.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import vn.vatm.contract.org.Role;

/**
 * Base for backend integration tests (T023): a real PostgreSQL via Testcontainers with Flyway
 * migrations applied, MockMvc, and helpers to authenticate as a federated SSO/AD user.
 *
 * <p>Uses the singleton-container pattern — one container started for the whole JVM and reused
 * across every test class/context (it is never stopped between classes; the JVM reaps it on exit).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  /** Authenticate as a user in {@code unitCode} holding the given roles. */
  protected RequestPostProcessor asUser(String subject, String unitCode, Role... roles) {
    List<GrantedAuthority> authorities =
        Arrays.stream(roles)
            .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.name()))
            .toList();
    List<String> groups = Arrays.stream(roles).map(Enum::name).toList();
    return jwt()
        .jwt(
            j ->
                j.subject(subject)
                    .claim("unit", unitCode)
                    .claim("name", subject)
                    .claim("email", subject + "@vatm.vn")
                    .claim("groups", groups))
        .authorities(authorities);
  }

  /** A complete, valid contract request body with the given unique number. */
  protected Map<String, Object> contractBody(String number) {
    Map<String, Object> body = new HashMap<>();
    body.put("contractNumber", number);
    body.put("name", "Hợp đồng " + number);
    body.put("type", "SERVICE");
    body.put("partyA", "VATM");
    body.put("partyB", "Đối tác " + number);
    body.put("value", 1000000);
    body.put("signDate", "2026-01-01");
    body.put("termEnd", "2026-12-31");
    body.put("personInChargeId", UUID.randomUUID().toString());
    return body;
  }

  /** Creates a contract as the given user and returns its id. */
  protected String createContract(RequestPostProcessor user, String number) throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/contracts")
                    .with(user)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(contractBody(number))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("id").asText();
  }

  /** Creates an official contract (requires TCT approval) as the given user and returns its id. */
  protected String createOfficialContract(RequestPostProcessor user, String number)
      throws Exception {
    Map<String, Object> body = contractBody(number);
    body.put("official", true);
    String response =
        mockMvc
            .perform(
                post("/api/v1/contracts")
                    .with(user)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("id").asText();
  }

  /** Performs a workflow action on a contract and returns the raw result for assertions. */
  protected ResultActions performWorkflow(
      RequestPostProcessor user, String contractId, String action, String reason) throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("action", action);
    if (reason != null) {
      body.put("reason", reason);
    }
    return mockMvc.perform(
        post("/api/v1/contracts/{id}/workflow", contractId)
            .with(user)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
  }
}
