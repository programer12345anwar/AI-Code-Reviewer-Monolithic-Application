package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.common.ApiResponse;
import com.anwar.aicodereview.dto.CodeSubmissionRequest;
import com.anwar.aicodereview.dto.CompareVersionsRequest;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/code", "/api/v1/code"})
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
    public ResponseEntity<ApiResponse<CodeSubmission>> uploadCode(@Valid @RequestBody CodeSubmissionRequest request) {
        log.info("Uploading code for user {}", request.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Submission created", codeService.createSubmission(request)));
    }

    @PostMapping("/analyze/{submissionId}")
    public ResponseEntity<ApiResponse<CodeVersion>> analyzeCode(@PathVariable UUID submissionId) {
        log.info("Analyzing latest version for submission {}", submissionId);
        return ResponseEntity.ok(ApiResponse.success("Analysis completed", versionService.analyzeLatestVersion(submissionId)));
    }

    @GetMapping("/version/{submissionId}")
    public ResponseEntity<ApiResponse<List<CodeVersion>>> getVersions(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getVersions(submissionId)));
    }

    @PostMapping("/version/{submissionId}/new")
    public ResponseEntity<ApiResponse<CodeVersion>> createNewVersion(@PathVariable UUID submissionId,
                                                                     @RequestBody(required = false) String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code content is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Version created", versionService.createNextVersion(submissionId, code)));
    }

    @GetMapping("/submissions/{userId}")
    public ResponseEntity<ApiResponse<List<CodeSubmission>>> getUserSubmissions(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(codeService.getUserSubmissions(userId)));
    }

    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<Map<String, CodeVersion>>> compareVersions(@Valid @RequestBody CompareVersionsRequest request) {
        Map<String, CodeVersion> result = versionService.compareVersions(request.versionA(), request.versionB());
        return ResponseEntity.ok(ApiResponse.success("Comparison completed", result));
    }
}
