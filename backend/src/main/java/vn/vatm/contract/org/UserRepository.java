package vn.vatm.contract.org;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByExternalSubject(String externalSubject);

  List<User> findByUnit_Id(UUID unitId);
}
