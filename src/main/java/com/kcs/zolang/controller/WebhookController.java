package com.kcs.zolang.controller;

import com.kcs.zolang.dto.global.ResponseDto;
import com.kcs.zolang.exception.CommonException;
import com.kcs.zolang.exception.ErrorCode;
import com.kcs.zolang.service.CICDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook")
@Slf4j
public class WebhookController {
    private final CICDService cicdService;

    @PostMapping("/api/v1/webhook")
    public ResponseDto<?> handleGithubWebhook(@RequestBody Map<String, Object> payload, @RequestHeader("X-GitHub-Event") String eventType) {
        log.info("Received webhook event: " + eventType);
        switch (eventType) {
            case "push":
                return handlePushEvent(payload, eventType);
            case "pull_request":
                return handlePullRequestEvent(payload, eventType);
            default:
                return ResponseDto.fail(new CommonException(ErrorCode.FAILED_CREATE_WEBHOOK));
        }
    }

    private ResponseDto<?> handlePushEvent(Map<String, Object> payload, String eventType) {
        // push 이벤트 처리 로직
        log.info("Processing push event with payload: " + payload);
        cicdService.handleGithubWebhook(payload, eventType);
        return ResponseDto.ok(null);
    }

    private ResponseDto<?> handlePullRequestEvent(Map<String, Object> payload, String eventType) {
        // pull request 이벤트 처리 로직
        log.info("Processing pull request event with payload: " + payload);
        cicdService.handleGithubWebhook(payload, eventType);
        return ResponseDto.ok(null);
    }
}
