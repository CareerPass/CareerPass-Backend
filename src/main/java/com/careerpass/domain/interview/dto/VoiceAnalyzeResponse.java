package com.careerpass.domain.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class VoiceAnalyzeResponse {
    private boolean ok;
    private String filename;
    private String text;
    private List<Segment> segments;  // null 가능
    private String summary;

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Segment {
        private double start;
        private double end;
        private String text;
    }
}