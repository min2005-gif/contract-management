package vn.vatm.contract.attachment;

import java.time.Instant;
import java.util.UUID;

/** Attachment metadata returned to clients. */
public record AttachmentResponse(
    UUID id,
    UUID contractId,
    String filename,
    String contentType,
    AttachmentKind kind,
    long sizeBytes,
    Instant uploadedAt) {

  public static AttachmentResponse from(Attachment a) {
    return new AttachmentResponse(
        a.getId(),
        a.getContractId(),
        a.getFilename(),
        a.getContentType(),
        a.getKind(),
        a.getSizeBytes(),
        a.getUploadedAt());
  }
}
