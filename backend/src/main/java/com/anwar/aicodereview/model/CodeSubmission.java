package com.anwar.aicodereview.model;

import jakarta.persistence.*;
<<<<<<< HEAD
=======
import lombok.Data;
>>>>>>> e4dc59351648e69e971e1cae1ba10ff0b8de3ae8

import java.time.LocalDateTime;
import java.util.UUID;

<<<<<<< HEAD
=======
@Data
>>>>>>> e4dc59351648e69e971e1cae1ba10ff0b8de3ae8
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

<<<<<<< HEAD
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
=======
>>>>>>> e4dc59351648e69e971e1cae1ba10ff0b8de3ae8
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
