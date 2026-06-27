CREATE TABLE IF NOT EXISTS code_submission (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_code_submission_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS code_version (
    id BINARY(16) NOT NULL,
    submission_id BINARY(16) NOT NULL,
    version_number INT NOT NULL,
    code LONGTEXT NOT NULL,
    analysis LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_code_version_submission
        FOREIGN KEY (submission_id) REFERENCES code_submission(id),
    INDEX idx_code_version_submission_id (submission_id),
    UNIQUE KEY uk_code_version_submission_version (submission_id, version_number)
);
