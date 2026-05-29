package com.anwar.aicodereview.service;

import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.repository.CodeSubmissionReporsitory;
import com.anwar.aicodereview.repository.CodeVersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class VersionService {

    @Autowired
    private CodeVersonRepository versionRepository;

    @Autowired
    private CodeSubmissionReporsitory submissionReporsitory;

    @Autowired
    private AIService aiService;

    public List<CodeVersion> getVersion(UUID submissionId) {
        return versionRepository.findBySubmissionId(submissionId);
    }

    public CodeVersion analyzeVersion(UUID versionId) {
        CodeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("version not found"));

        String analysis = aiService.analyzeCode(version.getCode());
        version.setAnalysis(analysis);
        return versionRepository.save(version);
    }

    public List<CodeVersion> getVersions(UUID submissionId) {
        return versionRepository.findBySubmissionId(submissionId);
    }

    public CodeVersion createNextVersion(UUID submissionId, String newCode) {

        CodeSubmission submission = submissionReporsitory.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submussion not found"));

        Optional<CodeVersion> latest = versionRepository.findTopBySubmissionIdOrderByVersionNumberDesc(submissionId);

        int nextVersion = latest.map(v -> v.getVersionNumber() + 1).orElse(1);

        CodeVersion version = new CodeVersion();
        version.setSubmission(submission);
        version.setVersionNumber(nextVersion);
        version.setCode(newCode);
        version.setAnalysis("Pending analysis...");

        return versionRepository.save(version);
    }


    public CodeVersion getVersionByVersionId(UUID versionId) {
        return versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("version not found"));
    }

}
