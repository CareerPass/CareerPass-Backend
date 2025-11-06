package com.careerpass.domain.feedback.exception;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(Long id) {
        super("Feedback not found: " + id);
    }
}