package vn.vatm.contract.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Publishes the API metadata and bearer security scheme (T020); see contracts/openapi.yaml. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI contractManagementOpenApi() {
    final String scheme = "bearerAuth";
    return new OpenAPI()
        .info(
            new Info()
                .title("VATM Contract Management API")
                .version("1.0.0")
                .description("Centralized contract management for TCT + subordinate units."))
        .addSecurityItem(new SecurityRequirement().addList(scheme))
        .schemaRequirement(
            scheme,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));
  }
}
