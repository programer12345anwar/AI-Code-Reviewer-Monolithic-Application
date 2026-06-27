package com.anwar.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CodeSubmissionRequest(
        @NotNull UUID userId,
        @NotBlank String filename,
        @NotBlank String language,
        @NotBlank String code
) {
}
