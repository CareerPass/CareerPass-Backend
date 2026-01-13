package com.careerpass.global.config;

import com.careerpass.domain.user.service.UserService;
import com.careerpass.global.auth.jwt.JwtAuthenticationFilter;
import com.careerpass.global.auth.jwt.JwtProperties;
import com.careerpass.global.auth.jwt.JwtTokenProvider;
import com.careerpass.global.auth.oauth.OAuthCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontBaseUrl;

    // ✅JWT 토큰이 담긴 쿠키 이름
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    // ✅ 액세스 토큰 만료시간 (원하면 조정)
    private static final long ACCESS_TOKEN_TTL_MS = 1000L * 60 * 60; // 1시간

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final OAuthCodeService oAuthCodeService;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtProperties, ACCESS_TOKEN_TTL_MS);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // ✅ OAuth2용으로 필요 시 세션 생성
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

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
                                "/logout-success",
                                // OAuth 코드 교환
                                "/auth/token",
                                // ✅ actuator 임시 오픈(원인 추적용)
                                "/actuator/health",
                                "/actuator/mappings"
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
                            String nickname = oidcUser.getGivenName(); // 없을 수도 있음.
                            String googleSub = oidcUser.getSubject(); // 구글 유저 고유 식별자

                            // ✅ 사용자 정보로 회원가입 또는 로그인 처리
                            userService.upsertGoogleUser(email, nickname, googleSub);

                            String jwt = jwtTokenProvider.createAccessToken(email);

                            Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, jwt);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(true);
                            cookie.setPath("/");
                            cookie.setMaxAge((int) (ACCESS_TOKEN_TTL_MS / 1000));

                            response.addCookie(cookie);
                            String code = oAuthCodeService.issue(email);
                            String redirectUrl = frontBaseUrl + "/auth/callback?code=" +
                                    URLEncoder.encode(code, StandardCharsets.UTF_8);
                            log.info("OAuth2 success: email={}, redirect={}", email, redirectUrl);
                            response.sendRedirect(redirectUrl);
                        })
                        .failureHandler((request, response, exception) -> {
                            // ✅ 실패 시 /login?error 같은 스프링 기본 경로로 보내지 말고,
                            // 우리가 통제 가능한 곳으로 보냄
                            String redirectUrl = frontBaseUrl + "/auth/callback?error=login_failed";
                            log.info("OAuth2 Login Failed: {}, redirect={}", exception.getMessage(), redirectUrl);
                            response.sendRedirect(redirectUrl);
                        })
                )

                // ✅ 인증 실패 시: 401 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Unauthorized\"}");
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
        config.setAllowedOrigins(List.of("https://career-pass-frontend.vercel.app")); // 프론트 배포 이후 수정 해야함.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ 쿠키 포함 필수

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
