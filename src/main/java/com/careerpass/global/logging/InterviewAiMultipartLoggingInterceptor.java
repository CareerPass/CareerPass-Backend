package com.careerpass.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InterviewAiMultipartLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.startsWith(contextPath) ? requestUri.substring(contextPath.length()) : requestUri;
        if (!"/api/feedback/interview/ai".equals(path)) {
            return true;
        }

        String headerContentType = request.getHeader("Content-Type");
        String contentType = request.getContentType();
        boolean isMultipart = request instanceof MultipartHttpServletRequest;

        log.info("[INTERVIEW_AI_MULTIPART] contentType={} headerContentType={} isMultipart={}",
                contentType, headerContentType, isMultipart);

        if (isMultipart) {
            MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
            Set<String> filePartNames = multipart.getFileMap().keySet();
            Set<String> paramNames = multipart.getParameterMap().keySet();
            Map<String, String> fileContentTypes = multipart.getFileMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> safeContentType(entry.getValue())
                    ));

            log.info("[INTERVIEW_AI_MULTIPART] fileParts={} fileContentTypes={} paramParts={}",
                    filePartNames, fileContentTypes, paramNames);

            try {
                List<String> partInfo = request.getParts().stream()
                        .map(this::formatPart)
                        .toList();
                log.info("[INTERVIEW_AI_MULTIPART] servletParts={}", partInfo);
            } catch (Exception e) {
                log.warn("[INTERVIEW_AI_MULTIPART] failedToReadParts error={}", e.toString());
            }
        }

        return true;
    }

    private String safeContentType(MultipartFile file) {
        if (file == null) {
            return "null";
        }
        return file.getContentType();
    }

    private String formatPart(Part part) {
        return part.getName() + ":" + part.getContentType();
    }
}
