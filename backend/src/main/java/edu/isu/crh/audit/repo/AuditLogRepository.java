package edu.isu.crh.audit.repo;

import edu.isu.crh.audit.model.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
  List<AuditLogEntity> findByRequestIdOrderByCreatedAtDesc(UUID requestId);
}
