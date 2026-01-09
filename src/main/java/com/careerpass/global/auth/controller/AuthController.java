package com.careerpass.global.auth.controller;

import com.careerpass.global.auth.jwt.JwtTokenProvider;
import com.careerpass.global.auth.oauth.OAuthCodeService;
import com.careerpass.global.error.ErrorCode;
import com.careerpass.global.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthCodeService oAuthCodeService;

    public record TokenRequest(String code) {}

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}

    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> exchangeToken(@RequestBody TokenRequest request) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            log.info("Token exchange failed: missing code");
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "code is required"));
        }

        return oAuthCodeService.consume(request.code())
                .<ResponseEntity<?>>map(email -> {
                    String token = jwtTokenProvider.createAccessToken(email);
                    log.info("Token exchange success for email={}", email);
                    return ResponseEntity.ok(new TokenResponse(token, "Bearer", 3600));
                })
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "invalid or expired code")));
    }
}
