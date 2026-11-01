package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.common.ApiResponse;
import com.anwar.aicodereview.dto.CodeSubmissionRequest;
import com.anwar.aicodereview.dto.CodeVersionCreateRequest;
import com.anwar.aicodereview.dto.CompareVersionsRequest;
import com.anwar.aicodereview.dto.SubmissionSummary;
import com.anwar.aicodereview.exception.ResourceNotFoundException;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/code", "/api/v1/code"})
@Validated
public class CodeReviewController {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewController.class);

    private final CodeService codeService;
    private final VersionService versionService;

    public CodeReviewController(CodeService codeService, VersionService versionService) {
        this.codeService = codeService;
        this.versionService = versionService;
    }

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> sayHello() {
        return ResponseEntity.ok(ApiResponse.success("Backend is running", "hey this is backend code"));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<CodeSubmission>> uploadCode(@AuthenticationPrincipal UUID userId,
                                                                  @Valid @RequestBody CodeSubmissionRequest request) {
        log.info("Uploading code for user {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Submission created", codeService.createSubmission(userId, request)));
    }

    @PostMapping("/analyze/{submissionId}")
    public ResponseEntity<ApiResponse<CodeVersion>> analyzeCode(@AuthenticationPrincipal UUID userId,
                                                                @PathVariable UUID submissionId) {
        log.info("Analyzing latest version for submission {}", submissionId);
        return ResponseEntity.ok(ApiResponse.success("Analysis completed",
                versionService.analyzeLatestVersion(submissionId, userId)));
    }

    @GetMapping("/version/{submissionId}")
    public ResponseEntity<ApiResponse<List<CodeVersion>>> getVersions(@AuthenticationPrincipal UUID userId,
                                                                       @PathVariable UUID submissionId) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getVersions(submissionId, userId)));
    }

    @PostMapping("/version/{submissionId}/new")
    public ResponseEntity<ApiResponse<CodeVersion>> createNewVersion(@AuthenticationPrincipal UUID userId,
                                                                     @PathVariable UUID submissionId,
                                                                     @Valid @RequestBody CodeVersionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Version created",
                        versionService.createNextVersion(submissionId, userId, request.code())));
    }

    @GetMapping({"/submissions/me", "/submissions/{userId}"})
    public ResponseEntity<ApiResponse<List<SubmissionSummary>>> getUserSubmissions(
            @AuthenticationPrincipal UUID authenticatedUserId,
            @PathVariable(name = "userId", required = false) UUID userId,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.Min(0)
            @jakarta.validation.constraints.Max(10000) int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1)
            @jakarta.validation.constraints.Max(100) int size) {
        if (userId != null && !userId.equals(authenticatedUserId)) {
            throw new ResourceNotFoundException("Submissions not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CodeSubmission> result = codeService.getUserSubmissions(authenticatedUserId, pageable);
        List<SubmissionSummary> submissions = result.getContent().stream()
                .map(submission -> new SubmissionSummary(submission.getId(), submission.getFilename(),
                        submission.getLanguage(), submission.getCreatedAt()))
                .toList();
        Map<String, Object> meta = Map.of(
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages());
        return ResponseEntity.ok(new ApiResponse<>(true, "Submissions retrieved", submissions, Instant.now(), meta));
    }

    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<Map<String, CodeVersion>>> compareVersions(
            @AuthenticationPrincipal UUID userId, @Valid @RequestBody CompareVersionsRequest request) {
        Map<String, CodeVersion> result = versionService.compareVersions(
                request.versionA(), request.versionB(), userId);
        return ResponseEntity.ok(ApiResponse.success("Comparison completed", result));
    }
}
