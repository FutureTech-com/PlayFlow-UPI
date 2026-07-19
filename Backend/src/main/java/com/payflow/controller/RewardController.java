package com.payflow.controller;

import com.payflow.dto.reward.RewardSummaryResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.RewardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards & Cashback", description = "Cashback earned on qualifying payments")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    @Operation(summary = "Get cashback/reward summary and history")
    public ResponseEntity<RewardSummaryResponse> summary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(rewardService.getSummary(principal.getUser()));
    }
}
