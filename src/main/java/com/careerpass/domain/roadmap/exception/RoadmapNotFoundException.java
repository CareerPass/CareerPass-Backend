package com.careerpass.domain.roadmap.exception;

public class RoadmapNotFoundException extends RuntimeException {
    public RoadmapNotFoundException(String message) {
        super(message);
    }
}