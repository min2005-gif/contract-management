package vn.vatm.contract.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vatm.contract.audit.AuditService;
import vn.vatm.contract.audit.SecurityEventLogger;
import vn.vatm.contract.config.AccessControl;
import vn.vatm.contract.config.ApiExceptions.IntegrationUnavailableException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.contract.ContractResponse;
import vn.vatm.contract.integration.adapters.AccountingGateway;
import vn.vatm.contract.integration.adapters.EDocumentGateway;
import vn.vatm.contract.integration.adapters.SignatureGateway;

/**
 * Orchestrates the internal-system integrations for US5: digital signature, e-document linking, and
 * accounting reconciliation. All work through internal gateways only (FR-022). A signing failure
 * leaves the contract unchanged (no partial state) and surfaces as 502.
 */
@Service
public class IntegrationService {

  private final ContractRepository contracts;
  private final AccessControl accessControl;
  private final CurrentUserService currentUserService;
  private final AuditService audit;
  private final SecurityEventLogger securityLog;
  private final SignatureGateway signatureGateway;
  private final AccountingGateway accountingGateway;
  private final EDocumentGateway eDocumentGateway;

  public IntegrationService(
      ContractRepository contracts,
      AccessControl accessControl,
      CurrentUserService currentUserService,
      AuditService audit,
      SecurityEventLogger securityLog,
      SignatureGateway signatureGateway,
      AccountingGateway accountingGateway,
      EDocumentGateway eDocumentGateway) {
    this.contracts = contracts;
    this.accessControl = accessControl;
    this.currentUserService = currentUserService;
    this.audit = audit;
    this.securityLog = securityLog;
    this.signatureGateway = signatureGateway;
    this.accountingGateway = accountingGateway;
    this.eDocumentGateway = eDocumentGateway;
  }

  @Transactional
  public ContractResponse sign(UUID contractId) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());

    // Call the external service BEFORE mutating: a failure leaves the contract untouched.
    String signedRef;
    try {
      signedRef = signatureGateway.sign(contract);
    } catch (IntegrationUnavailableException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new IntegrationUnavailableException(
          "Dịch vụ chữ ký số không khả dụng. / Digital-signature service is unavailable.", e);
    }

    contract.setSigned(true);
    contract.setSignedDocumentRef(signedRef);
    contract.setUpdatedBy(user.userId());
    contracts.saveAndFlush(contract);

    Map<String, Object> summary = new HashMap<>();
    summary.put("signedDocumentRef", signedRef);
    audit.record(user.userId(), "CONTRACT_SIGN", "Contract", contractId, summary);
    return ContractResponse.from(contract);
  }

  @Transactional
  public ReconciliationResult reconcile(UUID contractId) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());

    ReconciliationResult result = accountingGateway.reconcile(contract);
    securityLog.log(user.userId(), "ACCOUNTING_RECONCILE", "contract=" + contractId);
    return result;
  }

  @Transactional
  public ContractResponse linkDocument(UUID contractId, String documentRef) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());

    String storedRef = eDocumentGateway.link(contractId, documentRef);

    Map<String, Object> extra = new HashMap<>(contract.getExtraFields());
    List<Object> docs =
        extra.get("eDocuments") instanceof List<?> existing
            ? new ArrayList<>(existing)
            : new ArrayList<>();
    docs.add(storedRef);
    extra.put("eDocuments", docs);
    contract.setExtraFields(extra);
    contract.setUpdatedBy(user.userId());
    contracts.saveAndFlush(contract);

    Map<String, Object> summary = new HashMap<>();
    summary.put("eDocumentRef", storedRef);
    audit.record(user.userId(), "CONTRACT_EDOC_LINK", "Contract", contractId, summary);
    return ContractResponse.from(contract);
  }

  private Contract requireContract(UUID contractId) {
    return contracts
        .findById(contractId)
        .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng. / Contract not found."));
  }
}
