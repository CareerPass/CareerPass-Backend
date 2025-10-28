package com.careerpass.domain.certificate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_user_certificate")
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDateTime acquisitionDate;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;
}
