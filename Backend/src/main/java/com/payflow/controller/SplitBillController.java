package com.payflow.controller;

import com.payflow.dto.splitbill.CreateSplitBillRequest;
import com.payflow.dto.splitbill.MyShareResponse;
import com.payflow.dto.splitbill.SplitBillResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.SplitBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/split-bills")
@RequiredArgsConstructor
@Tag(name = "Split Bills", description = "Split a bill among multiple PayFlow users")
public class SplitBillController {

    private final SplitBillService splitBillService;

    @PostMapping
    @Operation(summary = "Create a split bill and assign shares to participants by VPA")
    public ResponseEntity<SplitBillResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody CreateSplitBillRequest request) {
        return ResponseEntity.ok(splitBillService.create(principal.getUser(), request));
    }

    @GetMapping("/organized")
    @Operation(summary = "List split bills you created")
    public ResponseEntity<List<SplitBillResponse>> organized(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(splitBillService.listOrganized(principal.getUser()));
    }

    @GetMapping("/my-shares")
    @Operation(summary = "List your unpaid/paid shares across split bills")
    public ResponseEntity<List<MyShareResponse>> myShares(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(splitBillService.listMyShares(principal.getUser()));
    }

    @PostMapping("/shares/{shareId}/settle")
    @Operation(summary = "Pay your share of a split bill (requires transaction PIN)")
    public ResponseEntity<SplitBillResponse> settle(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable String shareId,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(splitBillService.settleShare(principal.getUser(), shareId, body.get("transactionPin")));
    }
}