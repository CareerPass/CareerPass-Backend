package com.careerpass.domain.user.repository;

import com.careerpass.domain.user.entity.SocialType;
import com.careerpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository
 * - 이메일 고유성 체크 및 조회용 메서드 제공
 * - 엔티티는 name, email, major, targetJob 4개 필드 기준
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 소셜 타입 + 소셜 번호로 단건 조회 (OAuth2 로그인/회원 생성 시용)
    Optional<User> findBySocialTypeAndSocialNumber(SocialType socialType, String socialNumber);

    // 🔹 이메일 존재 여부 (회원 생성 시 중복 체크용)
    boolean existsByEmail(String email);

    // 🔹 이메일로 단건 조회 (로그인 연동/ /me용)
    Optional<User> findByEmail(String email);

    // 👉 불필요한 메서드는 추가하지 않음 (CRUD는 JpaRepository 기본 제공)
}