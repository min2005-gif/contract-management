package vn.vatm.contract.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.admin.UserAdminDtos.UserUpsert;
import vn.vatm.contract.admin.UserAdminDtos.UserView;

/** Admin user-management endpoints (admin-only). */
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

  private final UserAdminService service;

  public UserAdminController(UserAdminService service) {
    this.service = service;
  }

  @GetMapping
  public List<UserView> list() {
    return service.list();
  }

  @PostMapping
  public UserView create(@Valid @RequestBody UserUpsert request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public UserView update(@PathVariable UUID id, @Valid @RequestBody UserUpsert request) {
    return service.update(id, request);
  }
}
