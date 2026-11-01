package com.anwar.aicodereview.repository;

import com.anwar.aicodereview.model.CodeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CodeVersonRepository extends JpaRepository<CodeVersion, UUID> {

    List<CodeVersion> findBySubmissionId(UUID submissionId);

    List<CodeVersion> findBySubmission_IdAndSubmission_UserIdOrderByVersionNumberAsc(
            UUID submissionId, UUID userId);

    Optional<CodeVersion> findTopBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    Optional<CodeVersion> findTopBySubmission_IdAndSubmission_UserIdOrderByVersionNumberDesc(
            UUID submissionId, UUID userId);

    Optional<CodeVersion> findByIdAndSubmission_UserId(UUID id, UUID userId);
}
