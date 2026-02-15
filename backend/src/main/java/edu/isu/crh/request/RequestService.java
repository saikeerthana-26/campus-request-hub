package edu.isu.crh.request;

import edu.isu.crh.audit.AuditService;
import edu.isu.crh.integrations.IntegrationClient;
import edu.isu.crh.request.dto.CreateRequestDto;
import edu.isu.crh.request.model.RequestEntity;
import edu.isu.crh.request.model.RequestStatus;
import edu.isu.crh.request.repo.RequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RequestService {
  private final RequestRepository repo;
  private final AuditService audit;
  private final IntegrationClient integrations;

  public RequestService(RequestRepository repo, AuditService audit, IntegrationClient integrations) {
    this.repo = repo;
    this.audit = audit;
    this.integrations = integrations;
  }

  public RequestEntity create(CreateRequestDto dto, String username) {
    RequestEntity e = new RequestEntity();
    e.setTitle(dto.title());
    e.setDescription(dto.description());
    e.setCategory(dto.category());
    e.setCreatedBy(username);
    e.setStatus(RequestStatus.DRAFT);

    RequestEntity saved = repo.save(e);
    audit.log(saved.getId(), username, "CREATE", "Created request in DRAFT");
    return saved;
  }

  public List<RequestEntity> listForUser(Authentication auth) {
    String user = auth.getName();
    boolean isApproverOrAdmin = hasAnyRole(auth, "ROLE_APPROVER", "ROLE_ADMIN");

    if (isApproverOrAdmin) {
      // Approver/Admin see SUBMITTED items (triage queue) + their own
      List<RequestEntity> queue = repo.findByStatusOrderByUpdatedAtDesc(RequestStatus.SUBMITTED);
      List<RequestEntity> mine = repo.findByCreatedByOrderByUpdatedAtDesc(user);
      // de-dupe
      Map<UUID, RequestEntity> map = new LinkedHashMap<>();
      for (RequestEntity r : queue) map.put(r.getId(), r);
      for (RequestEntity r : mine) map.put(r.getId(), r);
      return new ArrayList<>(map.values());
    }
    return repo.findByCreatedByOrderByUpdatedAtDesc(user);
  }

  public RequestEntity get(UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found"));
  }

  public Map<String, Object> getAudit(UUID id) {
    return Map.of("items", audit.get(id));
  }

  public Map<String, Object> getEmployeeProfile(String employeeId) {
    return integrations.fetchEmployee(employeeId);
  }

  public RequestEntity updateStatus(UUID id, RequestStatus target, Authentication auth) {
    RequestEntity e = get(id);
    String actor = auth.getName();

    // authorization rules by role
    boolean isEmployee = hasAnyRole(auth, "ROLE_EMPLOYEE");
    boolean isApprover = hasAnyRole(auth, "ROLE_APPROVER");
    boolean isAdmin = hasAnyRole(auth, "ROLE_ADMIN");

    // ownership rule for employee
    if (isEmployee && !e.getCreatedBy().equals(actor)) {
      throw new IllegalArgumentException("Employees can only modify their own requests");
    }

    RequestStatus from = e.getStatus();
    validateTransition(from, target, isEmployee, isApprover, isAdmin);

    e.setStatus(target);
    RequestEntity saved = repo.save(e);

    audit.log(id, actor, "STATUS_CHANGE", from + " -> " + target);

    // Integration: when approved, sync to legacy + webhook event
    if (target == RequestStatus.APPROVED) {
      integrations.syncToLegacy(Map.of(
          "requestId", id.toString(),
          "title", e.getTitle(),
          "category", e.getCategory(),
          "createdBy", e.getCreatedBy(),
          "approvedBy", actor
      ));

      integrations.sendWebhookEvent(Map.of(
          "type", "REQUEST_APPROVED",
          "requestId", id.toString(),
          "title", e.getTitle(),
          "approvedBy", actor
      ));
    }

    return saved;
  }

  private void validateTransition(RequestStatus from, RequestStatus to, boolean isEmployee, boolean isApprover, boolean isAdmin) {
    // base state machine
    Set<RequestStatus> allowedFromDraft = Set.of(RequestStatus.SUBMITTED, RequestStatus.DRAFT);
    Set<RequestStatus> allowedFromSubmitted = Set.of(RequestStatus.APPROVED, RequestStatus.REJECTED, RequestStatus.SUBMITTED);
    Set<RequestStatus> allowedFromApproved = Set.of(RequestStatus.COMPLETED, RequestStatus.APPROVED);
    Set<RequestStatus> allowedFromRejected = Set.of(RequestStatus.DRAFT, RequestStatus.REJECTED);
    Set<RequestStatus> allowedFromCompleted = Set.of(RequestStatus.COMPLETED);

    boolean allowedByState = switch (from) {
      case DRAFT -> allowedFromDraft.contains(to);
      case SUBMITTED -> allowedFromSubmitted.contains(to);
      case APPROVED -> allowedFromApproved.contains(to);
      case REJECTED -> allowedFromRejected.contains(to);
      case COMPLETED -> allowedFromCompleted.contains(to);
    };

    if (!allowedByState) throw new IllegalArgumentException("Invalid transition: " + from + " -> " + to);

    // role constraints
    if (isEmployee) {
      // employee can submit, or move rejected back to draft
      if (to == RequestStatus.APPROVED || to == RequestStatus.REJECTED || to == RequestStatus.COMPLETED) {
        throw new IllegalArgumentException("Employee cannot set status to " + to);
      }
      if (from == RequestStatus.SUBMITTED && to == RequestStatus.DRAFT) {
        throw new IllegalArgumentException("Cannot revert SUBMITTED back to DRAFT");
      }
    }

    if (isApprover && !isAdmin) {
      // approver cannot complete
      if (to == RequestStatus.COMPLETED) {
        throw new IllegalArgumentException("Approver cannot complete requests");
      }
    }
  }

  private boolean hasAnyRole(Authentication auth, String... roles) {
    return auth.getAuthorities().stream().anyMatch(a -> {
      for (String r : roles) if (a.getAuthority().equals(r)) return true;
      return false;
    });
  }
}
