package com.anwar.aicodereview.service;

import com.anwar.aicodereview.exception.ResourceNotFoundException;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.repository.CodeSubmissionReporsitory;
import com.anwar.aicodereview.repository.CodeVersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionService.class);
    private static final String PENDING_ANALYSIS = "Pending analysis...";

    private final CodeVersonRepository versionRepository;
    private final CodeSubmissionReporsitory submissionRepository;
    private final AiAnalysisService aiAnalysisService;

    public VersionService(CodeVersonRepository versionRepository,
                          CodeSubmissionReporsitory submissionRepository,
                          AiAnalysisService aiAnalysisService) {
        this.versionRepository = versionRepository;
        this.submissionRepository = submissionRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    public List<CodeVersion> getVersions(UUID submissionId) {
        return versionRepository.findBySubmissionId(submissionId);
    }

    @Transactional
    public CodeVersion analyzeLatestVersion(UUID submissionId) {
        List<CodeVersion> versions = versionRepository.findBySubmissionId(submissionId);
        Optional<CodeVersion> latest = versions.stream()
                .max(java.util.Comparator.comparingInt(CodeVersion::getVersionNumber));

        CodeVersion selectedVersion = latest.orElseThrow(() -> new ResourceNotFoundException("No versions found for submission: " + submissionId));
        return analyzeVersion(selectedVersion.getId());
    }

    @Transactional
    public CodeVersion analyzeVersion(UUID versionId) {
        CodeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId));

        String analysis = aiAnalysisService.analyzeCode(version.getCode());
        version.setAnalysis(analysis);
        log.info("Persisted analysis for version {}", versionId);
        return versionRepository.save(version);
    }

    @Transactional
    public CodeVersion createNextVersion(UUID submissionId, String newCode) {
        CodeSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        Optional<CodeVersion> latest = versionRepository.findTopBySubmissionIdOrderByVersionNumberDesc(submissionId);
        int nextVersion = latest.map(v -> v.getVersionNumber() + 1).orElse(1);

        CodeVersion version = new CodeVersion();
        version.setSubmission(submission);
        version.setVersionNumber(nextVersion);
        version.setCode(newCode);
        version.setAnalysis(PENDING_ANALYSIS);

        return versionRepository.save(version);
    }

    public CodeVersion getVersionByVersionId(UUID versionId) {
        return versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId));
    }

    public Map<String, CodeVersion> compareVersions(UUID versionAId, UUID versionBId) {
        CodeVersion versionA = getVersionByVersionId(versionAId);
        CodeVersion versionB = getVersionByVersionId(versionBId);
        Map<String, CodeVersion> result = new HashMap<>();
        result.put("versionA", versionA);
        result.put("versionB", versionB);
        return result;
    }
}
