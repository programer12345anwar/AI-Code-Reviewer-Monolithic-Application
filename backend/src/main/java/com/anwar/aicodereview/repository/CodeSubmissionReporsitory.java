package com.anwar.aicodereview.repository;

import com.anwar.aicodereview.model.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CodeSubmissionReporsitory extends JpaRepository<CodeSubmission, UUID> {

    Page<CodeSubmission> findByUserId(UUID userId, Pageable pageable);

    Optional<CodeSubmission> findByIdAndUserId(UUID id, UUID userId);

}
