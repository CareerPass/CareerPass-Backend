CREATE TABLE IF NOT EXISTS tb_interview_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_applied VARCHAR(100) NOT NULL,
    total_duration_sec BIGINT NULL,
    question_count INT NULL,
    average_score DOUBLE NULL,
    overall_strengths TEXT NULL,
    overall_improvements TEXT NULL,
    overall_risks TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interview_session_user_id ON tb_interview_session (user_id);

CREATE TABLE IF NOT EXISTS tb_interview_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    transcript TEXT NULL,
    score INT NULL,
    time_ms BIGINT NULL,
    strengths TEXT NULL,
    improvements TEXT NULL,
    risks TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_answer_session
        FOREIGN KEY (session_id) REFERENCES tb_interview_session(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_interview_answer_session_id ON tb_interview_answer (session_id);
