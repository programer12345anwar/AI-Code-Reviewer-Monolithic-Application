package com.anwar.aicodereview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubmissionSummary(UUID id, String filename, String language, LocalDateTime createdAt) {
}
