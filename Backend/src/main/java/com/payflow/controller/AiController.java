package com.payflow.controller;

import com.payflow.dto.ai.*;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.repository.TransactionRepository;
import com.payflow.security.UserPrincipal;
import com.payflow.service.ChatbotService;
import com.payflow.service.FraudDetectionService;
import com.payflow.service.SmartBudgetService;
import com.payflow.service.SpendingInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Features", description = "Rule-based expense categorization, spending insights, budget suggestions, chatbot, and fraud alerts - see class Javadoc in service.ai for how each works")
public class AiController {

    private final SpendingInsightsService spendingInsightsService;
    private final SmartBudgetService smartBudgetService;
    private final ChatbotService chatbotService;
    private final TransactionRepository transactionRepository;

    @GetMapping("/insights")
    @Operation(summary = "AI-generated spending insights and category breakdown for the current user")
    public ResponseEntity<SpendingInsightsResponse> insights(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(spendingInsightsService.getInsights(principal.getUser()));
    }

    @GetMapping("/budget-suggestions")
    @Operation(summary = "Smart monthly budget suggestions per category, based on trailing 3-month average spend")
    public ResponseEntity<BudgetSuggestionsResponse> budgetSuggestions(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(smartBudgetService.suggestBudgets(principal.getUser()));
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the PayFlow support chatbot (also used by the voice assistant)")
    public ResponseEntity<ChatResponse> chat(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatbotService.respond(principal.getUser(), request.getMessage()));
    }

    @GetMapping("/fraud-alerts")
    @Operation(summary = "This user's own transactions that were flagged as medium/high risk by fraud prediction")
    public ResponseEntity<List<FraudAlertResponse>> myFraudAlerts(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        List<FraudAlertResponse> alerts = transactionRepository
                .findBySenderAndRiskLevelInOrderByRiskScoreDesc(user, List.of(Transaction.RiskLevel.MEDIUM, Transaction.RiskLevel.HIGH))
                .stream()
                .map(t -> FraudDetectionService.toAlert(t, false))
                .toList();
        return ResponseEntity.ok(alerts);
    }
}