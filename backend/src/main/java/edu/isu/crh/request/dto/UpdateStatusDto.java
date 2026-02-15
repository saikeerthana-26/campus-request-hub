package edu.isu.crh.request.dto;

import edu.isu.crh.request.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDto(
    @NotNull RequestStatus status
) {}
