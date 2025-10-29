package com.careerpass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔒 CSRF (API 위주면 disable)
                .csrf(csrf -> csrf.disable())

                // ✅ 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/health", "/logout-success").permitAll()
                        .anyRequest().authenticated()
                )

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
}