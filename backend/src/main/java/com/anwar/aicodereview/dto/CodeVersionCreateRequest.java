package com.anwar.aicodereview.dto;

import com.anwar.aicodereview.common.CodeLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeVersionCreateRequest(@NotBlank @Size(max = CodeLimits.MAX_SOURCE_LENGTH) String code) {
}
