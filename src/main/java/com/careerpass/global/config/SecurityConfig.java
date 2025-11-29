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
                // ✅ CORS 활성화
                .cors(cors -> {})

                // 🔒 CSRF (API 위주면 disable)
                .csrf(csrf -> csrf.disable())

                // ✅ 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                // 루트
                                "/health",          // 단순 헬스
                                "/api/health",      // API 헬스
                                "/error",

                                // ✅ Swagger 관련 URL 전부 허용
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                // ✅ 로그아웃 성공 페이지
                                "/logout-success"
                        ).permitAll()

                        // 🧷 그 외 모든 요청은 로그인 필요
                        // /me, /api/users/**, /api/roadmap/**, /api/introductions/** 등 전부 포함
                        .anyRequest().authenticated()
                )

                // 폼로그인/기본 인증은 사용 안 함 (우린 OAuth2만)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ OAuth2 로그인 (구글)
                .oauth2Login(oauth -> oauth
                        // 로그인 성공 시 프론트 콜백으로 리다이렉트
                        .defaultSuccessUrl("http://localhost:3000/auth/callback", true)
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

    // ✅ 개발용 CORS (프론트 → API 호출 허용)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000"
                                // 배포 프론트 생기면 여기 추가
                                // "http://13.125.192.47:3000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}