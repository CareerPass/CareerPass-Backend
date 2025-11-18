package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.entity.InterviewLearningRecord;
import com.careerpass.domain.interview.entity.Question;
import com.careerpass.domain.interview.repository.InterviewLearningRecordRepository;
import com.careerpass.domain.interview.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 🎤 모의면접 학습 기록 서비스
 *
 * - 사용자가 실제 모의면접에서 음성 답변을 제출하거나
 *   Whisper 변환/AI 분석 결과가 생성되면
 *   이를 InterviewLearningRecord 형태로 DB에 저장하는 역할
 *
 * - "하나의 질문 → 하나의 답변" 형태로 연결되는 구조
 */
@Service
@RequiredArgsConstructor
public class InterviewLearningRecordService {

    private final InterviewLearningRecordRepository recordRepository;
    private final QuestionRepository questionRepository;

    /**
     * 🔹 모의면접 학습 기록 저장
     *
     * @param userId         답변한 사용자 ID
     * @param questionId     어떤 질문에 대한 답변인지 (FK)
     * @param audioUrl       녹음된 음성 파일 URL (S3 등)
     * @param answerText     Whisper 변환 텍스트
     * @param analysisResult AI 분석 결과 (피드백/점수/개선점)
     * @param durationMs     답변 시간(ms)
     * @return 저장된 InterviewLearningRecord 엔티티
     */
    public InterviewLearningRecord saveRecord(
            Long userId,
            Long questionId,
            String audioUrl,
            String answerText,
            String analysisResult,
            Long durationMs
    ) {
        // 🔍 1) 질문 존재 여부 검증
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("❌ 존재하지 않는 질문 ID입니다: " + questionId)
                );

        // 🏗 2) 엔티티 생성
        InterviewLearningRecord record = InterviewLearningRecord.builder()
                .userId(userId)
                .question(question)
                .audioUrl(audioUrl)
                .answerText(answerText)
                .analysisResult(analysisResult)
                .durationMs(durationMs)
                .build();

        // 💾 3) DB 저장
        return recordRepository.save(record);
    }
}