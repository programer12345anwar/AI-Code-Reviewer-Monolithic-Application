CREATE TABLE user_account (
    id UUID NOT NULL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE code_submission (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account(id),
    filename VARCHAR(255) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_code_submission_user_created_at ON code_submission (user_id, created_at DESC);

CREATE TABLE code_version (
    id UUID NOT NULL PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES code_submission(id),
    version_number INTEGER NOT NULL,
    code TEXT NOT NULL,
    analysis TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_code_version_submission_version UNIQUE (submission_id, version_number)
);

CREATE INDEX idx_code_version_submission_id ON code_version (submission_id);
