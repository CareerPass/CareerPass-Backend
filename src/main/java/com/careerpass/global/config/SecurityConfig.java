package com.careerpass.global.config;

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
                // CORS
                .cors(cors -> {})

                // CSRF (API 위주라 비활성화)
                .csrf(csrf -> csrf.disable())

                // 🔐 인가 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",              // 루트
                                "/health",
                                "/error",
                                // Swagger
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                // 프론트에서 호출하는 모든 API 임시 오픈
                                "/api/**",
                                // 혹시 쓸 수도 있는 /me 엔드포인트
                                "/me"
                        ).permitAll()
                        // 그 외는 인증 필요 (지금은 사실상 없음)
                        .anyRequest().authenticated()
                )

                // 폼 로그인/Basic 인증 사용 안 함
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

        // ✅ 서버 쪽 OAuth2 로그인은 잠시 끈다 (프론트에서만 처리할 거라서)
        // .oauth2Login(oauth -> {})   // <- 이 줄 완전히 제거 또는 주석
        ;

        return http.build();
    }

    // 개발용 CORS (프론트 → API 호출 허용)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                // 배포 전이라 그냥 전부 허용
                                "*"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)  // "*" 쓸 때는 false가 안전
                        .maxAge(3600);
            }
        };
    }
}