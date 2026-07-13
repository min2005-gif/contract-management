package vn.vatm.contract.contract;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContractRepository
    extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {

  boolean existsByOwningUnitIdAndContractNumber(UUID owningUnitId, String contractNumber);

  Optional<Contract> findByOwningUnitIdAndContractNumber(UUID owningUnitId, String contractNumber);
}
