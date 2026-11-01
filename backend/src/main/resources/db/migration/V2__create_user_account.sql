CREATE TABLE user_account (
    id BINARY(16) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_account_email UNIQUE (email)
);
