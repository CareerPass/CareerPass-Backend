ALTER TABLE tb_interview_session
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS';

UPDATE tb_interview_session
SET status = 'COMPLETED';
