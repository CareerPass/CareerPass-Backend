package com.careerpass.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    // 프론트 엔드 주소 (지금은 로컬 개발 기준)
    // 👉 프론트 dev 서버 주소로 맞춰줘 (npm run dev 쓰면 보통 5173)
    private static final String FRONT_BASE_URL = "http://localhost:5173";

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
                                // /me 엔드포인트
                                "/me",
                                // OAuth 관련 엔드포인트
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 폼 로그인/Basic 인증 사용 안 함
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ 서버 OAuth2 로그인 다시 활성화 + 성공 시 프론트로 리다이렉트
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                            String email = oidcUser.getEmail();

                            // email URL 인코딩
                            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

                            // 프론트로 리다이렉트 (쿼리에 email 넘겨줌)
                            String redirectUrl = FRONT_BASE_URL + "/?email=" + encodedEmail;
                            response.sendRedirect(redirectUrl);
                        })
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
                                "http://localhost:3000"       // 프론트 dev 주소
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}