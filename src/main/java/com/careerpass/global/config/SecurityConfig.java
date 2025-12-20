package com.careerpass.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    // ✅ 배포/개발 환경에 맞게 바꾸기 (우선 로컬)
    private static final String FRONT_BASE_URL = "http://localhost:3000";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS
                .cors(cors -> {})

                // CSRF (세션/쿠키를 쓸 거라면 운영에서는 재검토 필요)
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
                                // OAuth 관련 엔드포인트
                                "/oauth2/**",
                                "/login/oauth2/**",
                                // 로그아웃 성공 엔드포인트
                                "/logout-success"
                        ).permitAll()

                        // ✅ 로그인 된 사용자만 접근 가능
                        .requestMatchers("/me").authenticated()
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                // 폼 로그인/Basic 인증 사용 안 함
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ OAuth2 로그인 성공 시 프론트로 리다이렉트
                // ⚠️ 개인정보(email 등)를 URL 쿼리로 넘기지 않음 (로그/히스토리 유출 위험)
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            response.sendRedirect(FRONT_BASE_URL + "/");
                        })
                )

                .logout(logout -> logout
                    .logoutUrl("/logout")                 // 기본: POST /logout
                    .logoutSuccessUrl("/logout-success")  // 성공 시 이동
                    .invalidateHttpSession(true)          // 세션 무효화
                    .deleteCookies("JSESSIONID")          // 쿠키 제거
                    .clearAuthentication(true)
                        .permitAll()
                );

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
                                "http://localhost:3000" // 프론트 dev 주소
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}