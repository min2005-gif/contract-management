package vn.vatm.contract.org;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lists organizational units (for dropdowns / filters). */
@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

  private final OrganizationalUnitRepository units;

  public UnitController(OrganizationalUnitRepository units) {
    this.units = units;
  }

  @GetMapping
  public List<UnitDto> list() {
    return units.findAll().stream()
        .sorted((a, b) -> a.getCode().compareTo(b.getCode()))
        .map(
            u -> new UnitDto(u.getId(), u.getCode(), u.getName(), u.getType().name(), u.isActive()))
        .toList();
  }

  public record UnitDto(UUID id, String code, String name, String type, boolean active) {}
}
