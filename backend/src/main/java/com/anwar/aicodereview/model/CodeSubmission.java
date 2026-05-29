package com.anwar.aicodereview.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "code_submission")
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String filename;
    private String language;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected  void onCreate() {
        createdAt = LocalDateTime.now();
    }

}

/*
CREATE TABLE code_submission (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    filename VARCHAR(255),
    language VARCHAR(255),
    code TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id)
) ;
 */
