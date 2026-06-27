package com.anwar.aicodereview.service;

import com.anwar.aicodereview.dto.CodeSubmissionRequest;
import com.anwar.aicodereview.exception.ResourceNotFoundException;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.repository.CodeSubmissionReporsitory;
import com.anwar.aicodereview.repository.CodeVersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CodeService {

    private static final Logger log = LoggerFactory.getLogger(CodeService.class);

    private static final int INITIAL_VERSION_NUMBER = 1;
    private static final String PENDING_ANALYSIS = "Pending analysis...";

    private final CodeSubmissionReporsitory submissionRepository;
    private final CodeVersonRepository versionRepository;

    public CodeService(CodeSubmissionReporsitory submissionRepository, CodeVersonRepository versionRepository) {
        this.submissionRepository = submissionRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public CodeSubmission createSubmission(CodeSubmissionRequest request) {
        CodeSubmission submission = new CodeSubmission();
        submission.setUserId(request.userId());
        submission.setFilename(request.filename());
        submission.setLanguage(request.language());
        submission.setCode(request.code());

        CodeSubmission savedSubmission = submissionRepository.save(submission);
        log.info("Created submission {} for user {}", savedSubmission.getId(), request.userId());

        CodeVersion initialVersion = new CodeVersion();
        initialVersion.setSubmission(savedSubmission);
        initialVersion.setVersionNumber(INITIAL_VERSION_NUMBER);
        initialVersion.setCode(request.code());
        initialVersion.setAnalysis(PENDING_ANALYSIS);
        versionRepository.save(initialVersion);

        return savedSubmission;
    }

    public List<CodeSubmission> getUserSubmissions(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        return submissionRepository.findByUserId(userId);
    }

    public CodeSubmission getSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
    }
}
