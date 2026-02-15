package edu.isu.crh.audit.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UUID requestId;

  @Column(nullable = false, length = 80)
  private String actor;

  @Column(nullable = false, length = 60)
  private String action;

  @Column(columnDefinition = "text")
  private String details;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
  }

  // getters/setters
  public Long getId() { return id; }

  public UUID getRequestId() { return requestId; }
  public void setRequestId(UUID requestId) { this.requestId = requestId; }

  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public String getDetails() { return details; }
  public void setDetails(String details) { this.details = details; }

  public Instant getCreatedAt() { return createdAt; }
}
