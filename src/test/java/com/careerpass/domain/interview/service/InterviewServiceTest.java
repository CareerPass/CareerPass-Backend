package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.repository.InterviewJpaRepository;
import com.careerpass.global.aws.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    @Test
    @DisplayName("✅ 정상 업로드 → S3 저장 후 JPA save 호출")
    void create_ok() throws IOException {
        InterviewJpaRepository repo = Mockito.mock(InterviewJpaRepository.class);
        S3Service s3 = Mockito.mock(S3Service.class);
        InterviewService svc = new InterviewService(repo, s3);

        MockMultipartFile file = new MockMultipartFile(
                "file","ok.wav","audio/wav","x".getBytes());

        when(s3.storeFile(file)).thenReturn("s3://bucket/key.wav");
        when(repo.save(any())).thenAnswer(inv -> {
            Interview i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        Interview saved = svc.createInterview(42L, "Backend", file);

        assertNotNull(saved.getId());
        assertEquals("Backend", saved.getJobApplied());
        verify(s3, times(1)).storeFile(file);
        verify(repo, times(1)).save(any());
    }

    @Test
    @DisplayName("🟥 S3 업로드 실패 → 예외 전파")
    void s3_fail_throws() throws IOException {
        InterviewJpaRepository repo = Mockito.mock(InterviewJpaRepository.class);
        S3Service s3 = Mockito.mock(S3Service.class);
        InterviewService svc = new InterviewService(repo, s3);

        MockMultipartFile file = new MockMultipartFile(
                "file","ok.wav","audio/wav","x".getBytes());

        when(s3.storeFile(file)).thenThrow(new IOException("S3 down"));

        assertThrows(IOException.class, () -> svc.createInterview(1L,"BE", file));
        verify(repo, never()).save(any());
    }
}