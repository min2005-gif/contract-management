package vn.vatm.contract.org;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

  List<UserRole> findByUserId(UUID userId);

  @Transactional
  void deleteByUserId(UUID userId);
}
