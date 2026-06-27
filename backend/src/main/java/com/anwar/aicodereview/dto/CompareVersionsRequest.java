package com.anwar.aicodereview.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CompareVersionsRequest(@NotNull UUID versionA, @NotNull UUID versionB) {
}
