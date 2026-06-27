package com.anwar.aicodereview.controller;

<<<<<<< HEAD
import com.anwar.aicodereview.common.ApiResponse;
import com.anwar.aicodereview.dto.CodeSubmissionRequest;
import com.anwar.aicodereview.dto.CodeVersionCreateRequest;
import com.anwar.aicodereview.dto.CompareVersionsRequest;
=======
>>>>>>> e4dc59351648e69e971e1cae1ba10ff0b8de3ae8
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
<<<<<<< HEAD
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Submission created", codeService.createSubmission(request)));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Version created", versionService.createNextVersion(submissionId, code)));
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
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/code")
public class CodeReviewController {

    @Autowired
    private CodeService codeService;

    @Autowired
    private VersionService versionService;

    @GetMapping("/hello")
    public String sayHello() {
        return "hey this is backend code";
    }

    @PostMapping("/upload")
    public ResponseEntity<CodeSubmission> uploadCode(@RequestBody CodeSubmission submission) {
        return ResponseEntity.ok(codeService.createSubmission(submission));
    }

    @PostMapping("/analyze/{submissionId}")
    public ResponseEntity<CodeVersion> analyzedCode(@PathVariable UUID submissionId) {

        Optional<CodeVersion> latest = versionService.getVersion(submissionId).stream()
                    .max(Comparator.comparingInt(CodeVersion::getVersionNumber));

        if(latest.isPresent()) {
            return ResponseEntity.ok(versionService.analyzeVersion(latest.get().getId()));
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/version/{submissionId}")
    public ResponseEntity<List<CodeVersion>> getVersions(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(versionService.getVersions(submissionId));
    }

    @PostMapping("/version/{submissionId}/new")
    public ResponseEntity<CodeVersion> createNewVersion(@PathVariable UUID submissionId, @RequestBody String code) {
        return ResponseEntity.ok(versionService.createNextVersion(submissionId, code));
    }

    @GetMapping("/submissions/{userId}")
    public ResponseEntity<List<CodeSubmission>> getUserSubmussions(@PathVariable UUID userId) {
        return ResponseEntity.ok(codeService.getUserSubmissions(userId));
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareVersions(@RequestBody Map<String, UUID> request) {

        UUID versionAId = request.get("versionA");
        UUID versionBId = request.get("versionB");

        CodeVersion vA = versionService.getVersionByVersionId(versionAId);
        CodeVersion vB= versionService.getVersionByVersionId(versionBId);

        Map<String, Object> result = new HashMap<>();
        result.put("versionA", vA);
        result.put("versionB", vB);

        return ResponseEntity.ok(result);
    }


>>>>>>> e4dc59351648e69e971e1cae1ba10ff0b8de3ae8
}
