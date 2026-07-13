package vn.vatm.contract.attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/** Metadata for a file attached to a contract (FR-010); the binary lives in object storage. */
@Entity
@Table(name = "attachment")
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "contract_id", nullable = false)
  private UUID contractId;

  @Column(nullable = false)
  private String filename;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttachmentKind kind;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "object_key", nullable = false)
  private String objectKey;

  @Column(name = "uploaded_by")
  private UUID uploadedBy;

  @CreationTimestamp
  @Column(name = "uploaded_at", updatable = false)
  private Instant uploadedAt;

  protected Attachment() {}

  public Attachment(
      UUID contractId,
      String filename,
      String contentType,
      AttachmentKind kind,
      long sizeBytes,
      String objectKey,
      UUID uploadedBy) {
    this.contractId = contractId;
    this.filename = filename;
    this.contentType = contentType;
    this.kind = kind;
    this.sizeBytes = sizeBytes;
    this.objectKey = objectKey;
    this.uploadedBy = uploadedBy;
  }

  public UUID getId() {
    return id;
  }

  public UUID getContractId() {
    return contractId;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public AttachmentKind getKind() {
    return kind;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public String getObjectKey() {
    return objectKey;
  }

  public UUID getUploadedBy() {
    return uploadedBy;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }
}
