package edu.isu.crh.request.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requests")
public class RequestEntity {

  @Id
  private UUID id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String description;

  @Column(nullable = false, length = 50)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private RequestStatus status;

  @Column(nullable = false, length = 80)
  private String createdBy;

  @Column(length = 80)
  private String assignedTo;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (status == null) status = RequestStatus.DRAFT;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  // getters/setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }

  public RequestStatus getStatus() { return status; }
  public void setStatus(RequestStatus status) { this.status = status; }

  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

  public String getAssignedTo() { return assignedTo; }
  public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
