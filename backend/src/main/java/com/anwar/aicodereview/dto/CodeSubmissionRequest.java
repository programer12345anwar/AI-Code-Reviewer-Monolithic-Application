package com.anwar.aicodereview.dto;

import com.anwar.aicodereview.common.CodeLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeSubmissionRequest(
        @NotBlank @Size(max = 255) String filename,
        @NotBlank @Size(max = 50) String language,
        @NotBlank @Size(max = CodeLimits.MAX_SOURCE_LENGTH) String code
) {
}
