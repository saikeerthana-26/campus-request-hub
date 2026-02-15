package edu.isu.crh.request;

import edu.isu.crh.request.dto.CreateRequestDto;
import edu.isu.crh.request.dto.UpdateStatusDto;
import edu.isu.crh.request.model.RequestEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
  private final RequestService service;

  public RequestController(RequestService service) {
    this.service = service;
  }

  @PostMapping
  public RequestEntity create(@Valid @RequestBody CreateRequestDto dto, Authentication auth) {
    return service.create(dto, auth.getName());
  }

  @GetMapping
  public List<RequestEntity> list(Authentication auth) {
    return service.listForUser(auth);
  }

  @GetMapping("/{id}")
  public RequestEntity get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping("/{id}/audit")
  public Map<String, Object> audit(@PathVariable UUID id) {
    return service.getAudit(id);
  }

  @PostMapping("/{id}/status")
  public RequestEntity updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusDto dto, Authentication auth) {
    return service.updateStatus(id, dto.status(), auth);
  }

  // Integration demo: fetch employee profile from legacy service
  @GetMapping("/legacy/employee/{employeeId}")
  public Map<String, Object> legacyEmployee(@PathVariable String employeeId) {
    return service.getEmployeeProfile(employeeId);
  }
}
