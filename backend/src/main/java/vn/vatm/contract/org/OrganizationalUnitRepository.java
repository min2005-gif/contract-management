package vn.vatm.contract.org;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, UUID> {

  Optional<OrganizationalUnit> findByCode(String code);
}
