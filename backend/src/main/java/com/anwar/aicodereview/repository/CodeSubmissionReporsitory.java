package com.anwar.aicodereview.repository;

import com.anwar.aicodereview.model.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CodeSubmissionReporsitory extends JpaRepository<CodeSubmission, UUID> {
    // select * from tableName where userId='x'
    List<CodeSubmission> findByUserId(UUID userId);
}
