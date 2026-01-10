package com.careerpass.global.config;

import com.careerpass.global.logging.InterviewAiMultipartLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final InterviewAiMultipartLoggingInterceptor interviewAiMultipartLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interviewAiMultipartLoggingInterceptor)
                .addPathPatterns("/api/feedback/interview/ai");
    }
}
