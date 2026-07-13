package vn.vatm.contract.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * The central contract aggregate (FR-007, FR-008). The listed fields are the minimum; additional
 * fields live in {@link #extraFields} (JSONB) so the schema can grow (FR-009). Contract numbers are
 * unique within the owning unit (FR-026); {@link #version} provides optimistic concurrency.
 */
@Entity
@Table(name = "contract")
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "contract_number", nullable = false)
  private String contractNumber;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContractType type;

  @Column(name = "party_a", nullable = false)
  private String partyA;

  @Column(name = "party_b", nullable = false)
  private String partyB;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal value;

  @Column(name = "sign_date", nullable = false)
  private LocalDate signDate;

  @Column(name = "term_end", nullable = false)
  private LocalDate termEnd;

  @Column(name = "person_in_charge_id", nullable = false)
  private UUID personInChargeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContractStatus status = ContractStatus.DRAFT;

  @Column(name = "is_official", nullable = false)
  private boolean official = false;

  @Column(name = "owning_unit_id", nullable = false)
  private UUID owningUnitId;

  @Column(nullable = false)
  private boolean signed = false;

  @Column(name = "signed_document_ref")
  private String signedDocumentRef;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

  @Column(name = "progress_pct", nullable = false)
  private int progressPct = 0;

  @Column(name = "expected_progress_pct", nullable = false)
  private int expectedProgressPct = 0;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extra_fields", columnDefinition = "jsonb", nullable = false)
  private Map<String, Object> extraFields = new HashMap<>();

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public String getContractNumber() {
    return contractNumber;
  }

  public void setContractNumber(String contractNumber) {
    this.contractNumber = contractNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ContractType getType() {
    return type;
  }

  public void setType(ContractType type) {
    this.type = type;
  }

  public String getPartyA() {
    return partyA;
  }

  public void setPartyA(String partyA) {
    this.partyA = partyA;
  }

  public String getPartyB() {
    return partyB;
  }

  public void setPartyB(String partyB) {
    this.partyB = partyB;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public LocalDate getSignDate() {
    return signDate;
  }

  public void setSignDate(LocalDate signDate) {
    this.signDate = signDate;
  }

  public LocalDate getTermEnd() {
    return termEnd;
  }

  public void setTermEnd(LocalDate termEnd) {
    this.termEnd = termEnd;
  }

  public UUID getPersonInChargeId() {
    return personInChargeId;
  }

  public void setPersonInChargeId(UUID personInChargeId) {
    this.personInChargeId = personInChargeId;
  }

  public ContractStatus getStatus() {
    return status;
  }

  public void setStatus(ContractStatus status) {
    this.status = status;
  }

  public boolean isOfficial() {
    return official;
  }

  public void setOfficial(boolean official) {
    this.official = official;
  }

  public UUID getOwningUnitId() {
    return owningUnitId;
  }

  public void setOwningUnitId(UUID owningUnitId) {
    this.owningUnitId = owningUnitId;
  }

  public boolean isSigned() {
    return signed;
  }

  public void setSigned(boolean signed) {
    this.signed = signed;
  }

  public String getSignedDocumentRef() {
    return signedDocumentRef;
  }

  public void setSignedDocumentRef(String signedDocumentRef) {
    this.signedDocumentRef = signedDocumentRef;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public int getProgressPct() {
    return progressPct;
  }

  public void setProgressPct(int progressPct) {
    this.progressPct = progressPct;
  }

  public int getExpectedProgressPct() {
    return expectedProgressPct;
  }

  public void setExpectedProgressPct(int expectedProgressPct) {
    this.expectedProgressPct = expectedProgressPct;
  }

  public Map<String, Object> getExtraFields() {
    return extraFields;
  }

  public void setExtraFields(Map<String, Object> extraFields) {
    this.extraFields = extraFields == null ? new HashMap<>() : extraFields;
  }

  public long getVersion() {
    return version;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
  }

  public UUID getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(UUID updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
