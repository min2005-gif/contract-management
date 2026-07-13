package vn.vatm.contract.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ContractRepository
    extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {

  boolean existsByOwningUnitIdAndContractNumber(UUID owningUnitId, String contractNumber);

  Optional<Contract> findByOwningUnitIdAndContractNumber(UUID owningUnitId, String contractNumber);

  /** Non-terminal contracts eligible for alert evaluation (excludes the given status). */
  List<Contract> findByStatusNot(ContractStatus status);

  // --- Reporting aggregates (US3) ---

  @Query("select coalesce(sum(c.value), 0) from Contract c")
  BigDecimal sumAllValue();

  long countByTermEndBetween(LocalDate start, LocalDate end);

  long countByStatusIn(Collection<ContractStatus> statuses);

  /** Rows of {@code [owningUnitId (UUID), count (Long), sumValue (BigDecimal)]} per unit. */
  @Query(
      "select c.owningUnitId, count(c), coalesce(sum(c.value), 0) from Contract c"
          + " group by c.owningUnitId")
  List<Object[]> aggregateByUnit();
}
