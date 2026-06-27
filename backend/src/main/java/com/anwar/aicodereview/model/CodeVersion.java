package com.anwar.aicodereview.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CodeSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(CodeSubmission submission) {
        this.submission = submission;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
