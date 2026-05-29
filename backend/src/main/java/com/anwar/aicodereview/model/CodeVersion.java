package com.anwar.aicodereview.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "code_version")
public class CodeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private CodeSubmission submission;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String analysis;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected  void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

/*
CREATE TABLE code_version (
        id BINARY(16) NOT NULL,
submission_id BINARY(16) NOT NULL,
version_number INT,
code TEXT,
analysis TEXT,
created_at DATETIME(6),
PRIMARY KEY (id),
CONSTRAINT fk_code_version_submission
FOREIGN KEY (submission_id)
REFERENCES code_submission(id)
);
 */
