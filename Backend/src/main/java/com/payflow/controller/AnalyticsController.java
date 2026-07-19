package com.payflow.controller;

import com.payflow.dto.analytics.SpendingAnalyticsResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Monthly spending analytics / expense tracker")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spending")
    @Operation(summary = "Get monthly spending analytics for the current user")
    public ResponseEntity<SpendingAnalyticsResponse> spending(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getAnalytics(principal.getUser()));
    }
}
