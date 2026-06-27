package com.anwar.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;

public record CodeVersionCreateRequest(@NotBlank String code) {
}
