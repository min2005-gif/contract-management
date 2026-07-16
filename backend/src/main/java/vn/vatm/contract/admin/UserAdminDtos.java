package vn.vatm.contract.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import vn.vatm.contract.org.Role;

/** DTOs for admin user management. */
public final class UserAdminDtos {

  private UserAdminDtos() {}

  /** A user as shown in the admin console. */
  public record UserView(
      UUID id,
      String externalSubject,
      String fullName,
      String email,
      UUID unitId,
      String unitCode,
      boolean active,
      List<Role> roles) {}

  /** Create or update a user. */
  public record UserUpsert(
      @NotBlank String externalSubject,
      String fullName,
      String email,
      @NotBlank String unitCode,
      Boolean active,
      @NotNull List<Role> roles) {}
}
