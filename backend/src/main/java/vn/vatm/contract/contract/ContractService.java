package vn.vatm.contract.contract;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.audit.AuditService;
import vn.vatm.contract.config.AccessControl;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ConflictException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;

/**
 * Contract use cases for US1: create (as DRAFT), update, read, and unit-scoped search. Enforces
 * owning-unit access (FR-003/FR-004), per-unit contract-number uniqueness (FR-026), required-field
 * and date validation (FR-011), and emits a business-audit record for each mutation (FR-015).
 */
@Service
public class ContractService {

  private final ContractRepository contracts;
  private final AccessControl accessControl;
  private final AuditService audit;
  private final CurrentUserService currentUserService;

  public ContractService(
      ContractRepository contracts,
      AccessControl accessControl,
      AuditService audit,
      CurrentUserService currentUserService) {
    this.contracts = contracts;
    this.accessControl = accessControl;
    this.audit = audit;
    this.currentUserService = currentUserService;
  }

  @Transactional
  public ContractResponse create(ContractInput in) {
    CurrentUser user = currentUserService.require();
    UUID owningUnitId = user.unitId();
    validateDates(in);
    if (contracts.existsByOwningUnitIdAndContractNumber(owningUnitId, in.contractNumber())) {
      throw new ConflictException(
          "Số hợp đồng đã tồn tại trong đơn vị: "
              + in.contractNumber()
              + " / Contract number already exists in this unit.");
    }
    Contract c = new Contract();
    c.setOwningUnitId(owningUnitId);
    c.setStatus(ContractStatus.DRAFT);
    applyInput(c, in);
    c.setCreatedBy(user.userId());
    c.setUpdatedBy(user.userId());
    Contract saved = contracts.saveAndFlush(c);
    audit.record(user.userId(), "CONTRACT_CREATE", "Contract", saved.getId(), summary(saved));
    return ContractResponse.from(saved);
  }

  @Transactional
  public ContractResponse update(UUID id, ContractInput in) {
    CurrentUser user = currentUserService.require();
    Contract c = contracts.findById(id).orElseThrow(this::notFound);
    accessControl.requireUnitAccess(user, c.getOwningUnitId());
    validateDates(in);
    if (!c.getContractNumber().equals(in.contractNumber())
        && contracts.existsByOwningUnitIdAndContractNumber(
            c.getOwningUnitId(), in.contractNumber())) {
      throw new ConflictException(
          "Số hợp đồng đã tồn tại trong đơn vị: "
              + in.contractNumber()
              + " / Contract number already exists in this unit.");
    }
    applyInput(c, in);
    c.setUpdatedBy(user.userId());
    Contract saved = contracts.saveAndFlush(c);
    audit.record(user.userId(), "CONTRACT_UPDATE", "Contract", saved.getId(), summary(saved));
    return ContractResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public ContractResponse get(UUID id) {
    CurrentUser user = currentUserService.require();
    Contract c = contracts.findById(id).orElseThrow(this::notFound);
    accessControl.requireUnitAccess(user, c.getOwningUnitId());
    return ContractResponse.from(c);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContractResponse> search(
      UUID unitIdFilter, ContractStatus status, ContractType type, String q, int page, int size) {
    CurrentUser user = currentUserService.require();
    UUID scope;
    if (user.canSeeAllUnits()) {
      scope = unitIdFilter; // may be null → all units
    } else {
      scope = user.unitId(); // forced to own unit
    }
    Pageable pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size);
    Page<Contract> result =
        contracts.findAll(filter(scope, status, type, blankToNull(q)), pageable);
    List<ContractResponse> content = result.map(ContractResponse::from).getContent();
    return PageResponse.of(result, content);
  }

  /** Builds a dynamic predicate, adding a clause only for each non-null filter. */
  private Specification<Contract> filter(
      UUID owningUnitId, ContractStatus status, ContractType type, String q) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (owningUnitId != null) {
        predicates.add(cb.equal(root.get("owningUnitId"), owningUnitId));
      }
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (type != null) {
        predicates.add(cb.equal(root.get("type"), type));
      }
      if (q != null) {
        String like = "%" + q.toLowerCase() + "%";
        predicates.add(
            cb.or(
                cb.like(cb.lower(root.get("contractNumber")), like),
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("partyA")), like),
                cb.like(cb.lower(root.get("partyB")), like)));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private void applyInput(Contract c, ContractInput in) {
    c.setContractNumber(in.contractNumber());
    c.setName(in.name());
    c.setType(in.type());
    c.setPartyA(in.partyA());
    c.setPartyB(in.partyB());
    c.setValue(in.value());
    c.setSignDate(in.signDate());
    c.setTermEnd(in.termEnd());
    c.setPersonInChargeId(in.personInChargeId());
    if (in.official() != null) {
      c.setOfficial(in.official());
    }
    if (in.extraFields() != null) {
      c.setExtraFields(in.extraFields());
    }
  }

  private void validateDates(ContractInput in) {
    if (in.termEnd().isBefore(in.signDate())) {
      throw new BadRequestException(
          "Thời hạn kết thúc phải sau hoặc bằng ngày ký. / Term end must be on or after sign date.");
    }
  }

  private Map<String, Object> summary(Contract c) {
    Map<String, Object> m = new HashMap<>();
    m.put("contractNumber", c.getContractNumber());
    m.put("name", c.getName());
    m.put("status", c.getStatus().name());
    m.put("value", c.getValue());
    return m;
  }

  private String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }

  private NotFoundException notFound() {
    return new NotFoundException("Không tìm thấy hợp đồng. / Contract not found.");
  }
}
