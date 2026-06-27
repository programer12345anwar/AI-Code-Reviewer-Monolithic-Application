package com.anwar.aicodereview.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(String error, String message, List<String> details, Instant timestamp) {
}
