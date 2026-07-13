package vn.vatm.contract.attachment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.vatm.contract.audit.AuditService;
import vn.vatm.contract.audit.SecurityEventLogger;
import vn.vatm.contract.config.AccessControl;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;
import vn.vatm.contract.config.CurrentUser;
import vn.vatm.contract.config.CurrentUserService;
import vn.vatm.contract.contract.Contract;
import vn.vatm.contract.contract.ContractRepository;
import vn.vatm.contract.storage.StorageService;

/**
 * Attachment use cases for US1: upload (with content-type allowlist and max-size validation per
 * FR-010), list, download, and delete. Access is bound to the owning contract's unit; downloads
 * emit a security event (FR-025) and mutations emit a business-audit record (FR-015).
 */
@Service
public class AttachmentService {

  /** Allowed MIME types: PDF, Word (.doc/.docx), and scanned images (jpg/png/tif). */
  private static final Set<String> ALLOWED_TYPES =
      Set.of(
          "application/pdf",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "image/jpeg",
          "image/png",
          "image/tiff");

  private final AttachmentRepository attachments;
  private final ContractRepository contracts;
  private final StorageService storage;
  private final AccessControl accessControl;
  private final AuditService audit;
  private final SecurityEventLogger securityLog;
  private final CurrentUserService currentUserService;
  private final long maxSizeBytes;

  public AttachmentService(
      AttachmentRepository attachments,
      ContractRepository contracts,
      StorageService storage,
      AccessControl accessControl,
      AuditService audit,
      SecurityEventLogger securityLog,
      CurrentUserService currentUserService,
      @Value("${app.attachment.max-size-bytes}") long maxSizeBytes) {
    this.attachments = attachments;
    this.contracts = contracts;
    this.storage = storage;
    this.accessControl = accessControl;
    this.audit = audit;
    this.securityLog = securityLog;
    this.currentUserService = currentUserService;
    this.maxSizeBytes = maxSizeBytes;
  }

  @Transactional
  public AttachmentResponse upload(UUID contractId, MultipartFile file, AttachmentKind kind) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());

    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Tệp trống. / Empty file.");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BadRequestException(
          "Định dạng tệp không được hỗ trợ: "
              + contentType
              + " / Unsupported file type. Allowed: PDF, Word, scanned images.");
    }
    if (file.getSize() > maxSizeBytes) {
      throw new BadRequestException(
          "Tệp vượt quá kích thước cho phép ("
              + maxSizeBytes
              + " bytes). / Attachment exceeds the allowed size.");
    }
    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new BadRequestException("Không thể đọc tệp. / Could not read file.");
    }
    String objectKey = storage.put(file.getOriginalFilename(), bytes, contentType);
    Attachment saved =
        attachments.save(
            new Attachment(
                contractId,
                file.getOriginalFilename(),
                contentType,
                kind == null ? AttachmentKind.OTHER : kind,
                file.getSize(),
                objectKey,
                user.userId()));
    Map<String, Object> summary = new HashMap<>();
    summary.put("attachmentId", saved.getId().toString());
    summary.put("filename", saved.getFilename());
    audit.record(user.userId(), "CONTRACT_ATTACHMENT_ADD", "Contract", contractId, summary);
    return AttachmentResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public List<AttachmentResponse> list(UUID contractId) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());
    return attachments.findByContractIdOrderByUploadedAtAsc(contractId).stream()
        .map(AttachmentResponse::from)
        .toList();
  }

  @Transactional
  public DownloadedFile download(UUID contractId, UUID attachmentId) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());
    Attachment attachment = requireAttachment(contractId, attachmentId);
    byte[] content = storage.get(attachment.getObjectKey());
    securityLog.log(
        user.userId(),
        "ATTACHMENT_DOWNLOAD",
        "attachment=" + attachmentId + " contract=" + contractId);
    return new DownloadedFile(attachment.getFilename(), attachment.getContentType(), content);
  }

  @Transactional
  public void delete(UUID contractId, UUID attachmentId) {
    CurrentUser user = currentUserService.require();
    Contract contract = requireContract(contractId);
    accessControl.requireUnitAccess(user, contract.getOwningUnitId());
    Attachment attachment = requireAttachment(contractId, attachmentId);
    storage.delete(attachment.getObjectKey());
    attachments.delete(attachment);
    Map<String, Object> summary = new HashMap<>();
    summary.put("attachmentId", attachmentId.toString());
    audit.record(user.userId(), "CONTRACT_ATTACHMENT_DELETE", "Contract", contractId, summary);
  }

  private Contract requireContract(UUID contractId) {
    return contracts
        .findById(contractId)
        .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng. / Contract not found."));
  }

  private Attachment requireAttachment(UUID contractId, UUID attachmentId) {
    Attachment attachment =
        attachments
            .findById(attachmentId)
            .orElseThrow(
                () ->
                    new NotFoundException("Không tìm thấy tệp đính kèm. / Attachment not found."));
    if (!attachment.getContractId().equals(contractId)) {
      throw new NotFoundException("Không tìm thấy tệp đính kèm. / Attachment not found.");
    }
    return attachment;
  }

  /** In-memory carrier for a downloaded file. */
  public record DownloadedFile(String filename, String contentType, byte[] content) {}
}
