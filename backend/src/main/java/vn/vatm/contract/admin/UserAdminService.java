package vn.vatm.contract.admin;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.admin.UserAdminDtos.UserUpsert;
import vn.vatm.contract.admin.UserAdminDtos.UserView;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ConflictException;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.org.OrganizationalUnit;
import vn.vatm.contract.org.OrganizationalUnitRepository;
import vn.vatm.contract.org.Role;
import vn.vatm.contract.org.User;
import vn.vatm.contract.org.UserRepository;
import vn.vatm.contract.org.UserRole;
import vn.vatm.contract.org.UserRoleRepository;

/** Admin management of users, their unit, roles, and active state (admin-only). */
@Service
public class UserAdminService {

  private final UserRepository users;
  private final OrganizationalUnitRepository units;
  private final UserRoleRepository userRoles;
  private final CurrentUserService currentUserService;

  public UserAdminService(
      UserRepository users,
      OrganizationalUnitRepository units,
      UserRoleRepository userRoles,
      CurrentUserService currentUserService) {
    this.users = users;
    this.units = units;
    this.userRoles = userRoles;
    this.currentUserService = currentUserService;
  }

  @Transactional(readOnly = true)
  public List<UserView> list() {
    requireAdmin();
    return users.findAll().stream()
        .sorted((a, b) -> a.getExternalSubject().compareTo(b.getExternalSubject()))
        .map(this::toView)
        .toList();
  }

  @Transactional
  public UserView create(UserUpsert request) {
    requireAdmin();
    if (users.findByExternalSubject(request.externalSubject()).isPresent()) {
      throw new ConflictException(
          "Tài khoản đã tồn tại: " + request.externalSubject() + " / User already exists.");
    }
    OrganizationalUnit unit = unitByCode(request.unitCode());
    User user =
        users.save(new User(request.externalSubject(), request.fullName(), request.email(), unit));
    if (request.active() != null) {
      user.setActive(request.active());
    }
    replaceRoles(user.getId(), request.roles());
    return toView(user);
  }

  @Transactional
  public UserView update(UUID id, UserUpsert request) {
    requireAdmin();
    User user =
        users
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException("Không tìm thấy tài khoản. / User not found."));
    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setUnit(unitByCode(request.unitCode()));
    if (request.active() != null) {
      user.setActive(request.active());
    }
    users.save(user);
    replaceRoles(id, request.roles());
    return toView(user);
  }

  private void replaceRoles(UUID userId, List<Role> roles) {
    userRoles.deleteByUserId(userId);
    userRoles.flush();
    for (Role role : roles) {
      userRoles.save(new UserRole(userId, role, null));
    }
  }

  private OrganizationalUnit unitByCode(String code) {
    return units
        .findByCode(code)
        .orElseThrow(
            () -> new BadRequestException("Đơn vị không tồn tại: " + code + " / Unknown unit."));
  }

  private UserView toView(User user) {
    List<Role> roles =
        userRoles.findByUserId(user.getId()).stream().map(UserRole::getRole).toList();
    return new UserView(
        user.getId(),
        user.getExternalSubject(),
        user.getFullName(),
        user.getEmail(),
        user.getUnit().getId(),
        user.getUnit().getCode(),
        user.isActive(),
        roles);
  }

  private void requireAdmin() {
    CurrentUser user = currentUserService.require();
    if (!user.hasRole(Role.ADMIN)) {
      throw new ForbiddenException("Chỉ quản trị viên mới được quản lý người dùng. / Admin only.");
    }
  }
}
