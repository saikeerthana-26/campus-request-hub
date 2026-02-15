package edu.isu.crh.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestDto(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String description,
    @NotBlank @Size(max = 50) String category
) {}
