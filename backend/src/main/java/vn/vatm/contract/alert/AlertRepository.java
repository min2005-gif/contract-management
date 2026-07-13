package vn.vatm.contract.alert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlertRepository
    extends JpaRepository<Alert, UUID>, JpaSpecificationExecutor<Alert> {

  boolean existsByContractIdAndTypeAndStatus(UUID contractId, AlertType type, AlertStatus status);

  long countByContractIdAndTypeAndStatus(UUID contractId, AlertType type, AlertStatus status);

  List<Alert> findByContractId(UUID contractId);
}
