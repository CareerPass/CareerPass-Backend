ALTER TABLE tb_interview_session
    ADD COLUMN title VARCHAR(100) NOT NULL DEFAULT '면접';

UPDATE tb_interview_session
SET title = CONCAT('면접 ', id)
WHERE title = '면접';
