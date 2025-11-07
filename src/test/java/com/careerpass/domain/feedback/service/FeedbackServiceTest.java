package com.careerpass.domain.feedback.service;

import com.careerpass.domain.feedback.dto.FeedbackDtos;
import com.careerpass.domain.feedback.entity.Feedback;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.exception.FeedbackNotFoundException;
import com.careerpass.domain.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    FeedbackRepository feedbackRepository;

    @InjectMocks
    FeedbackService feedbackService;

    @Test
    @DisplayName("create() - 정상 저장 시 Response 반환")
    void create_ok() {
        var req = new FeedbackDtos.CreateRequest(
                FeedbackType.INTRODUCTION,
                88L,
                "좋아요",
                "내용 좋음",
                1L,
                null
        );

        var entity = Feedback.builder()
                .id(10L)
                .feedbackType(FeedbackType.INTRODUCTION)
                .totalScore(88L)
                .feedbackText("좋아요")
                .sectionFeedback("내용 좋음")
                .introductionId(1L)
                .build();

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(entity);

        var resp = feedbackService.create(req);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.feedbackType()).isEqualTo(FeedbackType.INTRODUCTION);
        assertThat(resp.totalScore()).isEqualTo(88L);
    }

    @Test
    @DisplayName("get() - 존재하는 ID면 Response 반환")
    void get_ok() {
        var entity = Feedback.builder()
                .id(1L)
                .feedbackType(FeedbackType.INTERVIEW)
                .totalScore(70L)
                .feedbackText("개선 필요")
                .sectionFeedback("문장 어색함")
                .interviewId(3L)
                .build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(entity));

        var resp = feedbackService.get(1L);

        assertThat(resp.feedbackText()).isEqualTo("개선 필요");
        assertThat(resp.feedbackType()).isEqualTo(FeedbackType.INTERVIEW);
    }

    @Test
    @DisplayName("get() - 존재하지 않으면 FeedbackNotFoundException 발생")
    void get_notFound() {
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feedbackService.get(999L))
                .isInstanceOf(FeedbackNotFoundException.class)
                .hasMessageContaining("Feedback not found");
    }
}