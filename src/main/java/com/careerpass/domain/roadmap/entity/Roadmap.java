package com.careerpass.domain.roadmap.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_roadmap")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roadmap_type", nullable = false)
    private RoadmapType roadmapType;

    @Column(name = "major", length = 20, nullable = false)
    private String major;

    @Column(name = "job", length = 20, nullable = false)
    private String job;

    @Column(name = "major_name", length = 20, nullable = true)
    private String majorName;

    @Column(name = "cert_name", length = 20, nullable = true)
    private String certName;

    @Column(name = "grade", nullable = false)
    private Long grade;



    @PrePersist
    @PreUpdate
    public void validateCase() {
        if(roadmapType == RoadmapType.MAJOR) {
            if(majorName == null || certName != null) {
                throw new IllegalStateException("전공과목이름이 필요하고, 자격증이름은 NULL이어야 한다.");
            }
        }else if(roadmapType == RoadmapType.CERTIFICATION) {
            if(certName == null || majorName != null) {
                throw new IllegalStateException("자격증이름이 필요하고, 전공과목이름은 NULL이어야 한다.");
            }
        }else {
            throw new IllegalStateException("roadMapType이 MAJOR 또는 CERTIFICATION이어야 한다.");
        }
    }
}
