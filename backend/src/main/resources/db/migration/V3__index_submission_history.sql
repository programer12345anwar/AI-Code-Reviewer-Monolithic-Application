CREATE INDEX idx_code_submission_user_created_at
    ON code_submission (user_id, created_at);
