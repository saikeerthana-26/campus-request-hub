package edu.isu.crh.request.repo;

import edu.isu.crh.request.model.RequestEntity;
import edu.isu.crh.request.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<RequestEntity, UUID> {
  List<RequestEntity> findByCreatedByOrderByUpdatedAtDesc(String createdBy);
  List<RequestEntity> findByStatusOrderByUpdatedAtDesc(RequestStatus status);
}
