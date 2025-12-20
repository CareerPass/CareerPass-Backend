package com.careerpass.global.config;

import com.careerpass.global.auth.jwt.JwtAuthenticationFilter;
import com.careerpass.global.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SecurityConfig {

    // ✅ 배포/개발 환경에 맞게 바꾸기 (우선 로컬)
    private static final String FRONT_BASE_URL = "http://localhost:3000";

    // ✅JWT 토큰이 담긴 쿠키 이름
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    // ✅ 액세스 토큰 만료시간 (원하면 조정)
    private static final long ACCESS_TOKEN_TTL_MS = 1000L * 60 * 60; // 1시간

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            // 🔒 배포 기준: 시크릿 없으면 서버 뜨면 안 됨 (사고 방지)
            throw new IllegalStateException("JWT_SECRET is missing or too short (min 32 chars).");
        }
        return new JwtTokenProvider(secret, ACCESS_TOKEN_TTL_MS);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // ✅ 세션 사용 안 함 (토큰 방식)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

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

                // ✅ JWT 인증 필터 등록 (쿠키에서 access_token 읽어서 인증 세팅)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, ACCESS_TOKEN_COOKIE),
                        UsernamePasswordAuthenticationFilter.class
                )

                // ✅ 구글 OAuth2 로그인 성공 시: JWT 발급 → HttpOnly 쿠키로 내려줌 → 프론트로 리다이렉트
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                            String email = oidcUser.getEmail();

                            String jwt = jwtTokenProvider.createAccessToken(email);

                            Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, jwt);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false); // ⚠️ HTTPS 배포면 true로 변경해야 함
                            cookie.setPath("/");
                            cookie.setMaxAge((int) (ACCESS_TOKEN_TTL_MS / 1000));

                            response.addCookie(cookie);
                            response.sendRedirect(FRONT_BASE_URL + "/");
                        })
                )

                // ✅ JWT 로그아웃: 쿠키 삭제로 처리
                .logout(logout -> logout
                        .logoutUrl("/logout") // 기본 POST /logout
                        .logoutSuccessUrl("/logout-success")
                        .deleteCookies(ACCESS_TOKEN_COOKIE)
                        .permitAll()
                );

        return http.build();
    }

    // ✅ Security에서 CORS 적용되도록 CorsConfigurationSource로 제공
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ 쿠키 포함 필수

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}