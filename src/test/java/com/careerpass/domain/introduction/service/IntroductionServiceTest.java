package com.careerpass.domain.introduction.service;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import com.careerpass.domain.introduction.repository.IntroductionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntroductionServiceTest {

    @Mock
    IntroductionRepository introductionRepository;

    @InjectMocks
    IntroductionService introductionService;

    @Test
    @DisplayName("create: submissionTime이 null이면 now()로 대체됨")
    void create_fillNow_whenNull() {
        CreateRequest req = new CreateRequest(1L, "백엔드", "내용", null);
        Introduction saved = Introduction.builder()
                .id(100L).userId(1L).jobApplied("백엔드").introText("내용")
                .submissionTime(LocalDateTime.now())
                .build();

        when(introductionRepository.save(any())).thenReturn(saved);

        Response res = introductionService.create(req);

        assertThat(res.id()).isEqualTo(100L);
        verify(introductionRepository).save(any());
    }

    @Test
    @DisplayName("get: 존재하지 않으면 예외 발생")
    void get_notFound() {
        when(introductionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> introductionService.get(999L))
                .isInstanceOf(IntroductionNotFoundException.class);
    }

    @Test
    @DisplayName("listByUser: 정렬된 리스트 반환")
    void listByUser_ok() {
        List<Introduction> list = List.of(
                Introduction.builder()
                        .id(1L).userId(1L).jobApplied("백엔드").introText("A")
                        .submissionTime(LocalDateTime.now()).build()
        );
        when(introductionRepository.findByUserIdOrderBySubmissionTimeDesc(1L))
                .thenReturn(list);

        List<Response> result = introductionService.listByUser(1L);

        assertThat(result).hasSize(1);
        verify(introductionRepository).findByUserIdOrderBySubmissionTimeDesc(1L);
    }
}