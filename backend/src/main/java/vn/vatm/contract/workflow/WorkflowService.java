package vn.vatm.contract.workflow;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.audit.AuditService;
import vn.vatm.contract.config.AccessControl;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ConflictException;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.contract.ContractResponse;
import vn.vatm.contract.contract.ContractStatus;
import vn.vatm.contract.org.Role;

/**
 * Drives the contract lifecycle for US2: Create → Check → Approve (unit head, then TCT for official
 * contracts) → Track → Liquidate, with reject-with-reason. Enforces the legal state machine
 * (FR-012), the official/threshold TCT-approval rule (FR-013), reject reasons (FR-014), owning-unit
 * access, and role permissions; every transition writes a {@link WorkflowStep} and a business-audit
 * record (FR-015).
 */
@Service
public class WorkflowService {

  private final ContractRepository contracts;
  private final WorkflowStepRepository steps;
  private final AccessControl accessControl;
  private final CurrentUserService currentUserService;
  private final AuditService audit;
  private final BigDecimal tctApprovalThreshold;

  public WorkflowService(
      ContractRepository contracts,
      WorkflowStepRepository steps,
      AccessControl accessControl,
      CurrentUserService currentUserService,
      AuditService audit,
      @Value("${app.workflow.tct-approval-threshold}") BigDecimal tctApprovalThreshold) {
    this.contracts = contracts;
    this.steps = steps;
    this.accessControl = accessControl;
    this.currentUserService = currentUserService;
    this.audit = audit;
    this.tctApprovalThreshold = tctApprovalThreshold;
  }

  @Transactional(readOnly = true)
  public java.util.List<WorkflowStepResponse> history(UUID contractId) {
    CurrentUser user = currentUserService.require();
    Contract contract =
        contracts
            .findById(contractId)
            .orElseThrow(
                () -> new NotFoundException("Không tìm thấy hợp đồng. / Contract not found."));
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());
    return steps.findByContractIdOrderByCreatedAtAsc(contractId).stream()
        .map(WorkflowStepResponse::from)
        .toList();
  }

  @Transactional
  public ContractResponse perform(UUID contractId, WorkflowActionRequest request) {
    CurrentUser user = currentUserService.require();
    Contract contract =
        contracts
            .findById(contractId)
            .orElseThrow(
                () -> new NotFoundException("Không tìm thấy hợp đồng. / Contract not found."));
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());

    ContractStatus from = contract.getStatus();
    ContractStatus to = nextStatus(user, contract, request);

    contract.setStatus(to);
    contract.setUpdatedBy(user.userId());
    contracts.saveAndFlush(contract);

    steps.save(
        new WorkflowStep(
            contractId, request.action(), from, to, user.userId(), trimToNull(request.reason())));

    Map<String, Object> summary = new HashMap<>();
    summary.put("action", request.action().name());
    summary.put("from", from.name());
    summary.put("to", to.name());
    if (trimToNull(request.reason()) != null) {
      summary.put("reason", request.reason());
    }
    audit.record(
        user.userId(), "CONTRACT_" + request.action().name(), "Contract", contractId, summary);

    return ContractResponse.from(contract);
  }

  /** Applies the state machine + role rules and returns the target status. */
  private ContractStatus nextStatus(
      CurrentUser user, Contract contract, WorkflowActionRequest request) {
    ContractStatus status = contract.getStatus();
    return switch (request.action()) {
      case SUBMIT -> {
        requireStatus(status, ContractStatus.DRAFT);
        requireAnyRole(user, Role.DATA_ENTRY, Role.UNIT_HEAD, Role.ADMIN);
        yield ContractStatus.PENDING_CHECK;
      }
      case CHECK_APPROVE -> {
        requireStatus(status, ContractStatus.PENDING_CHECK);
        requireAnyRole(user, Role.UNIT_HEAD, Role.ADMIN);
        yield requiresTctApproval(contract)
            ? ContractStatus.PENDING_TCT_APPROVAL
            : ContractStatus.ACTIVE;
      }
      case TCT_APPROVE -> {
        requireStatus(status, ContractStatus.PENDING_TCT_APPROVAL);
        requireTctApprover(user);
        yield ContractStatus.ACTIVE;
      }
      case REJECT -> {
        requireStatus(status, ContractStatus.PENDING_CHECK, ContractStatus.PENDING_TCT_APPROVAL);
        if (trimToNull(request.reason()) == null) {
          throw new BadRequestException("Cần nêu lý do từ chối. / A rejection reason is required.");
        }
        if (status == ContractStatus.PENDING_TCT_APPROVAL) {
          requireTctApprover(user);
        } else {
          requireAnyRole(user, Role.UNIT_HEAD, Role.ADMIN);
        }
        yield ContractStatus.DRAFT;
      }
      case LIQUIDATE -> {
        requireStatus(
            status, ContractStatus.ACTIVE, ContractStatus.IN_PROGRESS, ContractStatus.COMPLETED);
        // Access to the owning unit is already checked; liquidation needs an approver role.
        requireAnyRole(user, Role.UNIT_HEAD, Role.ADMIN);
        yield ContractStatus.LIQUIDATED;
      }
    };
  }

  /** Official flag or value at/above the configured threshold requires TCT approval (FR-013). */
  private boolean requiresTctApproval(Contract contract) {
    return contract.isOfficial() || contract.getValue().compareTo(tctApprovalThreshold) >= 0;
  }

  private void requireStatus(ContractStatus actual, ContractStatus... allowed) {
    Set<ContractStatus> allowedSet = EnumSet.noneOf(ContractStatus.class);
    for (ContractStatus s : allowed) {
      allowedSet.add(s);
    }
    if (!allowedSet.contains(actual)) {
      throw new ConflictException(
          "Không thể thực hiện thao tác ở trạng thái "
              + actual
              + ". / Illegal transition from status "
              + actual
              + ".");
    }
  }

  private void requireAnyRole(CurrentUser user, Role... roles) {
    if (!hasAny(user, roles)) {
      throw forbidden();
    }
  }

  private void requireTctApprover(CurrentUser user) {
    if (!user.isTct() || !hasAny(user, Role.UNIT_HEAD, Role.ADMIN)) {
      throw new ForbiddenException(
          "Chỉ TCT mới có quyền duyệt hợp đồng chính thức. / Only TCT may grant final approval.");
    }
  }

  private boolean hasAny(CurrentUser user, Role... roles) {
    for (Role r : roles) {
      if (user.hasRole(r)) {
        return true;
      }
    }
    return false;
  }

  private ForbiddenException forbidden() {
    return new ForbiddenException(
        "Bạn không có quyền thực hiện thao tác này. / You are not allowed to perform this action.");
  }

  private String trimToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
