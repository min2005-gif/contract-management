package vn.vatm.contract.org;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** An internal VATM user whose identity is federated from SSO/AD (FR-006). */
@Entity
@Table(name = "app_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "external_subject", nullable = false, unique = true)
  private String externalSubject;

  @Column(name = "full_name")
  private String fullName;

  private String email;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "unit_id", nullable = false)
  private OrganizationalUnit unit;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  protected User() {}

  public User(String externalSubject, String fullName, String email, OrganizationalUnit unit) {
    this.externalSubject = externalSubject;
    this.fullName = fullName;
    this.email = email;
    this.unit = unit;
  }

  public UUID getId() {
    return id;
  }

  public String getExternalSubject() {
    return externalSubject;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public OrganizationalUnit getUnit() {
    return unit;
  }

  public boolean isActive() {
    return active;
  }
}
