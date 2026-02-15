package edu.isu.crh.audit;

import edu.isu.crh.audit.model.AuditLogEntity;
import edu.isu.crh.audit.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {
  private final AuditLogRepository repo;

  public AuditService(AuditLogRepository repo) {
    this.repo = repo;
  }

  public void log(UUID requestId, String actor, String action, String details) {
    AuditLogEntity e = new AuditLogEntity();
    e.setRequestId(requestId);
    e.setActor(actor);
    e.setAction(action);
    e.setDetails(details);
    repo.save(e);
  }

  public List<AuditLogEntity> get(UUID requestId) {
    return repo.findByRequestIdOrderByCreatedAtDesc(requestId);
  }
}
