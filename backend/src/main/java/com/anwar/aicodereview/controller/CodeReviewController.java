package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
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


}
