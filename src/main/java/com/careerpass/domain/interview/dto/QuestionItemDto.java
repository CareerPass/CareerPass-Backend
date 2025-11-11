package com.careerpass.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionItemDto {
    private Long questionId;   // 서버가 부여(또는 외부 생성값)
    private String text;       // 질문 내용
    private Integer orderNo;   // 화면 노출 순서
}