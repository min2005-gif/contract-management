package vn.vatm.contract.org;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;

/**
 * Lists people that can be assigned as a contract's person-in-charge. Scoped to the caller's unit
 * (TCT/management see all units).
 */
@RestController
@RequestMapping("/api/v1/users")
public class PeopleController {

  private final UserRepository users;
  private final CurrentUserService currentUserService;

  public PeopleController(UserRepository users, CurrentUserService currentUserService) {
    this.users = users;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public List<PersonDto> list() {
    CurrentUser caller = currentUserService.require();
    List<User> people =
        caller.canSeeAllUnits() ? users.findAll() : users.findByUnit_Id(caller.unitId());
    return people.stream()
        .filter(User::isActive)
        .sorted(Comparator.comparing(u -> displayName(u).toLowerCase()))
        .map(
            u ->
                new PersonDto(
                    u.getId(), u.getExternalSubject(), displayName(u), u.getUnit().getCode()))
        .toList();
  }

  private String displayName(User u) {
    return (u.getFullName() != null && !u.getFullName().isBlank())
        ? u.getFullName()
        : u.getExternalSubject();
  }

  public record PersonDto(UUID id, String externalSubject, String fullName, String unitCode) {}
}
