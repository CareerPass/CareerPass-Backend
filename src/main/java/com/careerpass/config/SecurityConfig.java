package com.careerpass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // ADD: CORS 설정 사용하도록 활성화

                // 🔒 CSRF (API 위주면 disable)
                .csrf(csrf -> csrf.disable())

                // ✅ 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/health",
                                "/error",
                                // ✅ Swagger 관련 URL 전부 허용
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/introductions/**",
                                "/api/interview/voice/**",
                                "/api/interview/audio",
                                "/api/interview/question-gen/**"
                        ).permitAll()
                        // 🔓 스모크 테스트용으로 User API만 임시 오픈
                        .requestMatchers("/api/users/**").permitAll()
                        // 🔓 자기소개서 저장/조회 임시 오픈 (Swagger 테스트용)
                        .requestMatchers("/api/introductions/**").permitAll()
                        // 🔓 피드백 저장/조회 임시 오픈 (Swagger 테스트용)
                        .requestMatchers("/api/feedbacks/**").permitAll()
                        // 🔓 AI 음성면접 API 오픈
                        .requestMatchers("/api/interviews/voice/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 폼로그인/기본인증은 사용 안 함 (우린 OAuth2만)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ OAuth2 로그인 (구글 자동 플로우)
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/me", true)
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logout-success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // 개발용 CORS (Swagger/프론트 → API 호출 허용)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:8080",
                                "http://localhost:3000" // ADD: 프론트 로컬
                        )
                        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}